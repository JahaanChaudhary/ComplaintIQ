package com.complaintiq.analytics.dto;
import lombok.*;
import java.util.List;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SLAReportDTO {
    private Long totalComplaints; private Long onTimeCount; private Long warningCount;
    private Long breachedCount; private Double breachRatePercent; private List<SLABreachDetail> recentBreaches;
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SLABreachDetail { private String ticketId; private String urgency; private String agentName; private Double hoursOverdue; }
}
