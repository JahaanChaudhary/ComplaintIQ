package com.complaintiq.ai;

import com.complaintiq.ai.enums.*;
import com.complaintiq.complaint.Complaint;
import com.complaintiq.complaint.enums.*;
import com.complaintiq.customer.Customer;
import com.complaintiq.exception.AIAnalysisException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIAnalysisService {

    private final RestTemplate geminiRestTemplate;
    private final AIAnalysisRepository aiAnalysisRepository;
    private final PromptBuilderService promptBuilderService;
    private final ObjectMapper objectMapper;

    @Value("${app.gemini.api-key}")
    private String geminiApiKey;

    @Value("${app.gemini.model}")
    private String geminiModel;

    @Value("${app.gemini.api-url}")
    private String geminiApiUrl;

    @Transactional
    public AIAnalysis analyzeComplaint(Complaint complaint, Customer customer) {
        String ticketId = complaint.getTicketId();
        log.info("Starting Gemini analysis: ticketId={}", ticketId);
        AIAnalysisResult result;
        try {
            result = callGemini(complaint.getTitle(), complaint.getDescription(), customer, complaint.getOrderId(), ticketId);
        } catch (Exception ex) {
            log.error("Gemini analysis failed for ticketId={}: {}", ticketId, ex.getMessage());
            result = AIAnalysisResult.defaultFallback();
        }
        result.applyLegalThreatOverride();
        if (isVipCustomer(customer)) {
            result.applyVipUpgrade();
        }
        log.info("Gemini analysis complete: ticketId={} urgency={} category={} sentiment={}",
                ticketId, result.getUrgency(), result.getCategory(), result.getSentiment());
        return saveAnalysis(complaint, result);
    }

    private AIAnalysisResult callGemini(String title, String description, Customer customer, String orderId, String ticketId) {
        String fullPrompt = promptBuilderService.buildFullPrompt(title, description, customer, orderId);
        String url = String.format("%s/%s:generateContent?key=%s", geminiApiUrl, geminiModel, geminiApiKey);

        Map<String, Object> textPart = Map.of("text", fullPrompt);
        Map<String, Object> content = Map.of("parts", List.of(textPart));
        Map<String, Object> thinkingConfig = Map.of("thinkingBudget", 0);
        Map<String, Object> generationConfig = Map.of(
                "temperature", 0.2,
                "maxOutputTokens", 2048,
                "responseMimeType", "application/json",
                "thinkingConfig", thinkingConfig
        );
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(content),
                "generationConfig", generationConfig
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        String rawResponse;
        try {
            ResponseEntity<String> response = geminiRestTemplate.exchange(url, HttpMethod.POST, request, String.class);
            rawResponse = extractGeminiText(response.getBody());
        } catch (Exception ex) {
            throw new AIAnalysisException(ticketId, "Gemini API call failed: " + ex.getMessage(), ex);
        }
        return parseAIResponse(rawResponse, ticketId);
    }

    private String extractGeminiText(String fullResponse) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(fullResponse);
        JsonNode candidates = root.path("candidates");
        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts.isArray() && parts.size() > 0) {
                return parts.get(0).path("text").asText("");
            }
        }
        return "";
    }

    private AIAnalysisResult parseAIResponse(String rawResponse, String ticketId) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return AIAnalysisResult.defaultFallback();
        }
        try {
            String cleaned = rawResponse.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            }
            JsonNode root = objectMapper.readTree(cleaned);
            UrgencyLevel urgency = parseEnum(root, "urgency", UrgencyLevel.class, UrgencyLevel.MEDIUM);
            ComplaintCategory category = parseEnum(root, "category", ComplaintCategory.class, ComplaintCategory.OTHER);
            SentimentLevel sentiment = parseEnum(root, "sentiment", SentimentLevel.class, SentimentLevel.NEUTRAL);
            ComplaintIntent intent = parseEnum(root, "intent", ComplaintIntent.class, ComplaintIntent.INFORMATION);
            String summary = getTextNode(root, "summary", "Complaint requires review.");
            String suggestedAction = getTextNode(root, "suggestedAction", "Review complaint and take appropriate action.");
            double confidenceScore = root.has("confidenceScore") && root.get("confidenceScore").isNumber()
                    ? Math.max(0.0, Math.min(1.0, root.get("confidenceScore").asDouble(0.5)))
                    : 0.5;

            return AIAnalysisResult.builder()
                    .urgency(urgency)
                    .category(category)
                    .sentiment(sentiment)
                    .intent(intent)
                    .summary(summary)
                    .suggestedAction(suggestedAction)
                    .confidenceScore(confidenceScore)
                    .rawResponse(rawResponse)
                    .build();
        } catch (JsonProcessingException ex) {
            log.error("Failed to parse Gemini JSON for ticketId={}: {}", ticketId, ex.getMessage());
            AIAnalysisResult fallback = AIAnalysisResult.defaultFallback();
            fallback.setRawResponse(rawResponse);
            return fallback;
        } catch (Exception ex) {
            log.error("Unexpected parse error for ticketId={}: {}", ticketId, ex.getMessage());
            return AIAnalysisResult.defaultFallback();
        }
    }

    @Transactional
    public AIAnalysis saveAnalysis(Complaint complaint, AIAnalysisResult result) {
        AIAnalysis analysis = AIAnalysis.builder()
                .complaint(complaint)
                .urgency(result.getUrgency())
                .category(result.getCategory())
                .sentiment(result.getSentiment())
                .intent(result.getIntent())
                .summary(result.getSummary())
                .suggestedAction(result.getSuggestedAction())
                .confidenceScore(result.getConfidenceScore())
                .rawResponse(result.getRawResponse())
                .analyzedAt(LocalDateTime.now())
                .build();
        return aiAnalysisRepository.save(analysis);
    }

    @Transactional
    public AIAnalysis reAnalyzeComplaint(Complaint complaint, Customer customer) {
        aiAnalysisRepository.findByComplaintId(complaint.getId()).ifPresent(aiAnalysisRepository::delete);
        return analyzeComplaint(complaint, customer);
    }

    public AIAnalysisRepository getRepository() {
        return aiAnalysisRepository;
    }

    private <T extends Enum<T>> T parseEnum(JsonNode root, String field, Class<T> enumClass, T defaultValue) {
        if (!root.has(field) || root.get(field).isNull()) return defaultValue;
        try {
            return Enum.valueOf(enumClass, root.get(field).asText().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return defaultValue;
        }
    }

    private String getTextNode(JsonNode root, String field, String defaultValue) {
        if (!root.has(field) || root.get(field).isNull()) return defaultValue;
        String value = root.get(field).asText().trim();
        return value.isBlank() ? defaultValue : value;
    }

    private boolean isVipCustomer(Customer customer) {
        return customer.getTier() != null
                && (customer.getTier().name().equals("VIP") || customer.getTier().name().equals("PREMIUM"));
    }
}
