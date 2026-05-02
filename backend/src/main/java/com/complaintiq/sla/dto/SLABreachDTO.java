package com.complaintiq.sla.dto;
import com.complaintiq.complaint.enums.UrgencyLevel;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SLABreachDTO {
    private String ticketId; private UrgencyLevel urgency;
    private LocalDateTime slaDeadline; private LocalDateTime breachedAt;
    private Double hoursElapsed; private Double hoursOverdue;
    private String assignedAgentName; private String assignedAgentEmail;
}
