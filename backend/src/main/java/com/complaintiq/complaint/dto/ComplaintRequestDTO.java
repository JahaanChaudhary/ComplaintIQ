package com.complaintiq.complaint.dto;
import com.complaintiq.complaint.enums.ComplaintChannel;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplaintRequestDTO {
    @NotBlank(message="Customer email is required") @Email(message="Invalid email format") private String customerEmail;
    @NotBlank(message="Title is required") @Size(max=200,message="Title must not exceed 200 characters") private String title;
    @NotBlank(message="Description is required") @Size(min=20,max=2000,message="Description must be between 20 and 2000 characters") private String description;
    private String orderId;
    @NotNull(message="Channel is required") private ComplaintChannel channel;
    private String imageUrl;
}
