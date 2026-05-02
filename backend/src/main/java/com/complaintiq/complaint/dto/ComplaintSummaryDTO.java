package com.complaintiq.complaint.dto;
import com.complaintiq.complaint.enums.*;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplaintSummaryDTO {
    private Long id; private String ticketId; private String title;
    private ComplaintStatus status; private UrgencyLevel urgency; private String category;
    private String customerName; private String customerEmail; private String customerTier;
    private String assignedAgentName; private String departmentName;
    private SlaStatus slaStatus; private LocalDateTime slaDeadline; private LocalDateTime createdAt;
}
