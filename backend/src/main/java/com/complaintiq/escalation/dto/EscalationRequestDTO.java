package com.complaintiq.escalation.dto;
import com.complaintiq.escalation.enums.EscalationReason;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EscalationRequestDTO {
    @NotNull(message="Escalation reason is required") private EscalationReason reason;
    @Size(max=1000) private String notes;
}
