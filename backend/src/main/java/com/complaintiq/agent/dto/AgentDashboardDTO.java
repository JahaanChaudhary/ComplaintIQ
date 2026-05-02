package com.complaintiq.agent.dto;
import com.complaintiq.complaint.dto.ComplaintSummaryDTO;
import lombok.*;
import java.util.List;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AgentDashboardDTO {
    private Long agentId; private String agentName; private String departmentName; private String role;
    private Boolean isAvailable; private Integer currentLoad; private Integer maxLoad;
    private Long todayResolved; private Double avgResolutionTimeHours; private Long slaBreachCount;
    private Long totalResolved; private List<ComplaintSummaryDTO> assignedComplaints;
}
