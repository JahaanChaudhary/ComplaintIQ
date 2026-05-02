package com.complaintiq.complaint.dto;
import com.complaintiq.complaint.enums.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StatusUpdateDTO {
    @NotNull(message="Status is required") private ComplaintStatus status;
    private String note;
}
