package com.complaintiq.ai;
import com.complaintiq.ai.enums.*;
import com.complaintiq.complaint.Complaint;
import com.complaintiq.complaint.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="ai_analyses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AIAnalysis {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="complaint_id",nullable=false,unique=true) private Complaint complaint;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private UrgencyLevel urgency;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private ComplaintCategory category;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private SentimentLevel sentiment;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private ComplaintIntent intent;
    @Column(length=500) private String summary;
    @Column(name="suggested_action",length=500) private String suggestedAction;
    @Column(name="confidence_score") private Double confidenceScore;
    @Column(name="raw_response",columnDefinition="TEXT") private String rawResponse;
    @Column(name="analyzed_at",nullable=false) @Builder.Default private LocalDateTime analyzedAt = LocalDateTime.now();
}
