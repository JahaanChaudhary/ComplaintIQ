package com.complaintiq.complaint;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="complaint_activities",indexes={@Index(name="idx_activity_complaint_id",columnList="complaint_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplaintActivity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="complaint_id",nullable=false) private Long complaintId;
    @Column(nullable=false,length=200) private String action;
    @Column(name="performed_by",length=100) private String performedBy;
    @Column(length=500) private String notes;
    @Column(name="performed_at",nullable=false) @Builder.Default private LocalDateTime performedAt = LocalDateTime.now();
}
