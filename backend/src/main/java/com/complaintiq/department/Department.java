package com.complaintiq.department;
import com.complaintiq.common.BaseEntity;
import com.complaintiq.complaint.enums.ComplaintCategory;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="departments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Department extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable=false,unique=true) private ComplaintCategory category;
    @Column(name="head_agent_id") private Long headAgentId;
    @Column(length=500) private String description;
}
