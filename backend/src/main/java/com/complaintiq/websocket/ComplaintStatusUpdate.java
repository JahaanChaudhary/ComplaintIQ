package com.complaintiq.websocket;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplaintStatusUpdate {
    private String ticketId; private String newStatus; private String message; private String slaStatus;
    @Builder.Default private LocalDateTime timestamp = LocalDateTime.now();
}
