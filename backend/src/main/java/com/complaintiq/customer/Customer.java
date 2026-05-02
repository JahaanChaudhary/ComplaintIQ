package com.complaintiq.customer;
import com.complaintiq.common.BaseEntity;
import com.complaintiq.customer.enums.CustomerTier;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="customers",indexes={@Index(name="idx_customer_email",columnList="email")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Customer extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String name;
    @Column(unique=true,nullable=false) private String email;
    @Column private String phone;
    @Enumerated(EnumType.STRING) @Column(nullable=false) @Builder.Default private CustomerTier tier = CustomerTier.NORMAL;
    @Column(nullable=false) @Builder.Default private Integer totalComplaints = 0;
    @Column(nullable=false) @Builder.Default private Boolean isActive = true;
}
