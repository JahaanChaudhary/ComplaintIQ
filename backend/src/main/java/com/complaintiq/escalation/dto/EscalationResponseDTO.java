package com.complaintiq.escalation.dto;
import com.complaintiq.escalation.enums.EscalationReason;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EscalationResponseDTO {
    private Long id; private String ticketId; private String escalatedFromName;
    private String escalatedToName; private EscalationReason reason;
    private String notes; private LocalDateTime escalatedAt;
}
