package com.complaintiq.analytics.dto;
import lombok.*;
import java.util.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardStatsDTO {
    private Long totalComplaints; private Long openComplaints; private Long resolvedToday;
    private Long criticalOpen; private Long escalatedComplaints; private Double slaBreachRate;
    private Double avgResolutionTimeHours; private Double avgSatisfactionScore;
    private Map<String, Long> complaintsByCategory; private Map<String, Long> complaintsByUrgency;
    private Map<String, Long> complaintsByStatus; private List<AgentPerformanceDTO> topPerformingAgents;
}
