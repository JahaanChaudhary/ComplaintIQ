package com.complaintiq.complaint.dto;
import com.complaintiq.complaint.enums.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplaintFilterDTO {
    private ComplaintStatus status;
    private UrgencyLevel urgency;
    private ComplaintCategory category;
    private Long agentId;
    private Long customerId;
    @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) private LocalDate dateFrom;
    @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) private LocalDate dateTo;
    private String keyword;
    @Builder.Default private int page = 0;
    @Builder.Default private int size = 10;
    @Builder.Default private String sortBy = "createdAt";
    @Builder.Default private String sortDir = "desc";
}
