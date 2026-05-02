package com.complaintiq.resolution.dto;
import com.complaintiq.resolution.enums.ResolutionType;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResolutionRequestDTO {
    @NotBlank(message="Resolution note is required") @Size(max=2000) private String resolutionNote;
    @NotNull(message="Resolution type is required") private ResolutionType resolutionType;
}
