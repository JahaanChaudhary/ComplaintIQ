package com.complaintiq.sla;
import com.complaintiq.complaint.enums.UrgencyLevel;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="sla_configs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SLAConfig {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(name="urgency_level",unique=true,nullable=false) private UrgencyLevel urgencyLevel;
    @Column(name="resolution_hours",nullable=false) private Integer resolutionHours;
    @Column(name="warning_threshold_percent",nullable=false) @Builder.Default private Integer warningThresholdPercent = 75;
    @Column(name="escalation_chain",nullable=false) @Builder.Default private String escalationChain = "JUNIOR,SENIOR,TEAM_LEAD,MANAGER";
}
