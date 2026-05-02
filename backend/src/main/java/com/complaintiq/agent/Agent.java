package com.complaintiq.agent;
import com.complaintiq.agent.enums.AgentRole;
import com.complaintiq.common.BaseEntity;
import com.complaintiq.department.Department;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="agents",indexes={
    @Index(name="idx_agent_email",columnList="email"),
    @Index(name="idx_agent_dept_id",columnList="department_id"),
    @Index(name="idx_agent_available",columnList="is_available")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Agent extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String name;
    @Column(unique=true,nullable=false) private String email;
    @Column private String phone;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="department_id",nullable=false) private Department department;
    @Enumerated(EnumType.STRING) @Column(nullable=false) @Builder.Default private AgentRole role = AgentRole.JUNIOR;
    @Column(name="is_available",nullable=false) @Builder.Default private Boolean isAvailable = true;
    @Column(name="current_load",nullable=false) @Builder.Default private Integer currentLoad = 0;
    @Column(name="max_load",nullable=false) @Builder.Default private Integer maxLoad = 10;
    @Column(name="total_resolved",nullable=false) @Builder.Default private Integer totalResolved = 0;
    @Column(name="avg_resolution_time_hours") @Builder.Default private Double avgResolutionTimeHours = 0.0;
    @Column(name="user_id") private Long userId;
}
