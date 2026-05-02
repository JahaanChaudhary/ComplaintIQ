package com.complaintiq.agent.dto;
import com.complaintiq.agent.enums.AgentRole;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AgentResponseDTO {
    private Long id; private String name; private String email; private String phone;
    private AgentRole role; private String departmentName; private Long departmentId;
    private Boolean isAvailable; private Integer currentLoad; private Integer maxLoad;
    private Integer totalResolved; private Double avgResolutionTimeHours;
}
