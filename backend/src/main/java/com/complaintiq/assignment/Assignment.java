package com.complaintiq.assignment;
import com.complaintiq.agent.Agent;
import com.complaintiq.assignment.enums.AssignmentStatus;
import com.complaintiq.common.BaseEntity;
import com.complaintiq.complaint.Complaint;
import com.complaintiq.department.Department;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="assignments",indexes={
    @Index(name="idx_assignment_complaint_id",columnList="complaint_id"),
    @Index(name="idx_assignment_agent_id",columnList="agent_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Assignment extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="complaint_id",nullable=false,unique=true) private Complaint complaint;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="agent_id",nullable=false) private Agent agent;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="department_id",nullable=false) private Department department;
    @Enumerated(EnumType.STRING) @Column(nullable=false) @Builder.Default private AssignmentStatus status = AssignmentStatus.ASSIGNED;
    @Column(name="assigned_at",nullable=false) @Builder.Default private LocalDateTime assignedAt = LocalDateTime.now();
    @Column(name="reassigned_at") private LocalDateTime reassignedAt;
    @Column(name="reassign_reason",length=500) private String reassignReason;
}
