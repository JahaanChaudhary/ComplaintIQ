package com.complaintiq.assignment.dto;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssignmentReassignDTO {
    @NotNull(message="New agent ID is required") private Long newAgentId;
    @NotNull(message="Reason is required") @Size(min=10,max=500) private String reason;
}
