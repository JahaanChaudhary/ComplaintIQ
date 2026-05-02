package com.complaintiq.complaint.dto;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeedbackRequestDTO {
    @NotNull(message="Satisfaction score is required") @Min(value=1,message="Score must be at least 1") @Max(value=5,message="Score must not exceed 5") private Integer satisfactionScore;
    @Size(max=1000,message="Feedback comment must not exceed 1000 characters") private String feedbackComment;
}
