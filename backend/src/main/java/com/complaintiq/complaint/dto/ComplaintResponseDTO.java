package com.complaintiq.complaint.dto;
import com.complaintiq.complaint.enums.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplaintResponseDTO {
    private Long id; private String ticketId; private String title; private String description;
    private String orderId; private ComplaintStatus status; private ComplaintChannel channel; private String imageUrl;
    private CustomerInfo customer; private AIAnalysisInfo aiAnalysis; private AssignmentInfo assignment;
    private LocalDateTime slaDeadline; private SlaStatus slaStatus;
    private List<ActivityInfo> timeline;
    private LocalDateTime createdAt; private LocalDateTime updatedAt; private LocalDateTime resolvedAt;
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CustomerInfo { private Long id; private String name; private String email; private String tier; }
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AIAnalysisInfo { private String urgency; private String category; private String sentiment; private String intent; private String summary; private String suggestedAction; private Double confidenceScore; }
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AssignmentInfo { private Long agentId; private String agentName; private String departmentName; private LocalDateTime assignedAt; private String status; }
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ActivityInfo { private String action; private String performedBy; private String notes; private LocalDateTime performedAt; }
}
