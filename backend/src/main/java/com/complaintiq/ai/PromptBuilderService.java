package com.complaintiq.ai;
import com.complaintiq.customer.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Slf4j @Service
public class PromptBuilderService {
    private static final String SYSTEM_PROMPT = """
You are an AI assistant for a customer complaint management system.
Respond ONLY with valid JSON — no markdown, no code blocks, no explanation.
All enum values must be EXACTLY as specified — uppercase only.
If you detect LEGAL_THREAT in intent, set urgency to CRITICAL.
VALID VALUES:
- urgency: CRITICAL | HIGH | MEDIUM | LOW
- category: DELIVERY | PAYMENT | PRODUCT | REFUND | TECHNICAL | OTHER
- sentiment: FURIOUS | ANGRY | FRUSTRATED | NEUTRAL | CALM
- intent: REFUND | REPLACEMENT | INFORMATION | LEGAL_THREAT | ESCALATION
RESPOND WITH:
{"urgency":"HIGH","category":"DELIVERY","sentiment":"ANGRY","intent":"REFUND","summary":"One sentence.","suggestedAction":"One sentence.","confidenceScore":0.92}
""";
    public String buildAnalysisPrompt(String title, String description, Customer customer, String orderId) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following customer complaint:\n\n");
        prompt.append("Title: ").append(sanitize(title)).append("\n");
        prompt.append("Description: ").append(sanitize(description)).append("\n");
        if (orderId != null && !orderId.isBlank()) prompt.append("Order ID: ").append(sanitize(orderId)).append("\n");
        prompt.append("\nCustomer Tier: ").append(customer.getTier().name()).append("\n");
        prompt.append("Previous Complaints: ").append(customer.getTotalComplaints()).append("\n");
        prompt.append("\nCheck for legal threat keywords: 'lawyer', 'sue', 'court', 'legal action', 'attorney'\n");
        prompt.append("\nReturn the JSON now:");
        return prompt.toString();
    }
    public String getSystemPrompt() { return SYSTEM_PROMPT; }
    public String buildFullPrompt(String title, String description, Customer customer, String orderId) {
        return SYSTEM_PROMPT + "\n\n" + buildAnalysisPrompt(title, description, customer, orderId);
    }
    private String sanitize(String input) {
        if (input == null) return "";
        return input.replace("```","").replace("IGNORE PREVIOUS","").replace("ignore previous","").replace("system:","").replace("SYSTEM:","").trim().substring(0, Math.min(input.length(), 1500));
    }
}
