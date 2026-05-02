package com.complaintiq.auth;
import com.complaintiq.auth.enums.UserRole;
import com.complaintiq.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="app_users",indexes={@Index(name="idx_user_email",columnList="email")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AppUser extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String name;
    @Column(unique=true,nullable=false) private String email;
    @Column(nullable=false) private String password;
    @Enumerated(EnumType.STRING) @Column(nullable=false) @Builder.Default private UserRole role = UserRole.CUSTOMER;
    @Column(name="is_active",nullable=false) @Builder.Default private Boolean isActive = true;
    @Column(name="customer_id") private Long customerId;
    @Column(name="agent_id") private Long agentId;
}
