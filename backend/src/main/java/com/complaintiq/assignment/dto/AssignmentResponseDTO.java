package com.complaintiq.assignment.dto;
import com.complaintiq.assignment.enums.AssignmentStatus;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssignmentResponseDTO {
    private Long id; private String ticketId; private Long agentId; private String agentName;
    private Long departmentId; private String departmentName; private AssignmentStatus status;
    private LocalDateTime assignedAt; private LocalDateTime reassignedAt; private String reassignReason;
}
