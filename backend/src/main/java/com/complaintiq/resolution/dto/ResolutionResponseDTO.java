package com.complaintiq.resolution.dto;
import com.complaintiq.resolution.enums.ResolutionType;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResolutionResponseDTO {
    private Long id; private String ticketId; private String resolutionNote;
    private ResolutionType resolutionType; private String resolvedByName;
    private LocalDateTime resolvedAt; private Integer satisfactionScore; private String feedbackComment;
}
