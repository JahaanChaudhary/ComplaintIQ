package com.complaintiq.resolution;
import com.complaintiq.agent.Agent;
import com.complaintiq.common.BaseEntity;
import com.complaintiq.complaint.Complaint;
import com.complaintiq.resolution.enums.ResolutionType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="resolutions",indexes={@Index(name="idx_resolution_complaint_id",columnList="complaint_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Resolution extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="complaint_id",nullable=false,unique=true) private Complaint complaint;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="resolved_by_id",nullable=false) private Agent resolvedBy;
    @Column(name="resolution_note",nullable=false,length=2000) private String resolutionNote;
    @Column(name="resolved_at",nullable=false) @Builder.Default private LocalDateTime resolvedAt = LocalDateTime.now();
    @Enumerated(EnumType.STRING) @Column(name="resolution_type",nullable=false) private ResolutionType resolutionType;
    @Column(name="satisfaction_score") private Integer satisfactionScore;
    @Column(name="feedback_comment",length=1000) private String feedbackComment;
}
