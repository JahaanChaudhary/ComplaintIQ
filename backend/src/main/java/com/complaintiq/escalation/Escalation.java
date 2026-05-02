package com.complaintiq.escalation;
import com.complaintiq.agent.Agent;
import com.complaintiq.common.BaseEntity;
import com.complaintiq.complaint.Complaint;
import com.complaintiq.escalation.enums.EscalationReason;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="escalations",indexes={@Index(name="idx_escalation_complaint_id",columnList="complaint_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Escalation extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="complaint_id",nullable=false) private Complaint complaint;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="escalated_from_id") private Agent escalatedFrom;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="escalated_to_id",nullable=false) private Agent escalatedTo;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private EscalationReason reason;
    @Column(name="escalated_at",nullable=false) @Builder.Default private LocalDateTime escalatedAt = LocalDateTime.now();
    @Column(length=1000) private String notes;
}
