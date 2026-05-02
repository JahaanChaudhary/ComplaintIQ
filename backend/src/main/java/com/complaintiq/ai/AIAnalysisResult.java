package com.complaintiq.ai;
import com.complaintiq.ai.enums.*;
import com.complaintiq.complaint.enums.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AIAnalysisResult {
    private UrgencyLevel urgency; private ComplaintCategory category; private SentimentLevel sentiment;
    private ComplaintIntent intent; private String summary; private String suggestedAction;
    private Double confidenceScore; private String rawResponse;
    public static AIAnalysisResult defaultFallback() {
        return AIAnalysisResult.builder().urgency(UrgencyLevel.MEDIUM).category(ComplaintCategory.OTHER).sentiment(SentimentLevel.NEUTRAL).intent(ComplaintIntent.INFORMATION).summary("Unable to analyze complaint automatically.").suggestedAction("Please review this complaint manually.").confidenceScore(0.0).rawResponse("AI_ANALYSIS_FAILED").build();
    }
    public void applyLegalThreatOverride() {
        if (this.intent == ComplaintIntent.LEGAL_THREAT) { this.urgency = UrgencyLevel.CRITICAL; this.suggestedAction = "URGENT: Legal threat detected. Escalate to manager and legal team immediately."; }
    }
    public void applyVipUpgrade() {
        this.urgency = switch (this.urgency) { case LOW -> UrgencyLevel.MEDIUM; case MEDIUM -> UrgencyLevel.HIGH; case HIGH -> UrgencyLevel.CRITICAL; default -> this.urgency; };
    }
}
