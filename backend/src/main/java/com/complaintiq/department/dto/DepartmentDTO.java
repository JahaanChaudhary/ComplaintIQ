package com.complaintiq.department.dto;
import com.complaintiq.complaint.enums.ComplaintCategory;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DepartmentDTO {
    private Long id;
    @NotBlank(message="Department name is required") private String name;
    @NotNull(message="Category is required") private ComplaintCategory category;
    private String description; private Long headAgentId; private String headAgentName; private Long agentCount;
}
