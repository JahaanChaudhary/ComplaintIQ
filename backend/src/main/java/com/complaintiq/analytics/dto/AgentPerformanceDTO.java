package com.complaintiq.analytics.dto;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AgentPerformanceDTO {
    private Long agentId; private String agentName; private String departmentName; private String role;
    private Long totalResolved; private Double avgResolutionTimeHours; private Long slaBreachCount;
    private Double satisfactionScore; private Integer currentLoad; private Integer maxLoad;
}
