package com.complaintiq.complaint;
import com.complaintiq.common.BaseEntity;
import com.complaintiq.complaint.enums.*;
import com.complaintiq.customer.Customer;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="complaints",indexes={
    @Index(name="idx_complaint_ticket_id",columnList="ticket_id"),
    @Index(name="idx_complaint_status",columnList="status"),
    @Index(name="idx_complaint_urgency",columnList="urgency"),
    @Index(name="idx_complaint_customer_id",columnList="customer_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Complaint extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="ticket_id",unique=true,nullable=false) private String ticketId;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="customer_id",nullable=false) private Customer customer;
    @Column(nullable=false,length=200) private String title;
    @Column(nullable=false,length=2000) private String description;
    @Column(name="order_id") private String orderId;
    @Enumerated(EnumType.STRING) @Column(nullable=false) @Builder.Default private ComplaintChannel channel = ComplaintChannel.WEB;
    @Enumerated(EnumType.STRING) @Column(nullable=false) @Builder.Default private ComplaintStatus status = ComplaintStatus.OPEN;
    @Enumerated(EnumType.STRING) @Column private UrgencyLevel urgency;
    @Column(name="image_url") private String imageUrl;
    @Column(name="resolved_at") private LocalDateTime resolvedAt;
    @Column(name="sla_deadline") private LocalDateTime slaDeadline;
}
