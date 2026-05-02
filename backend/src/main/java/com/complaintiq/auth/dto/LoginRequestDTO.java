package com.complaintiq.auth.dto;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginRequestDTO {
    @NotBlank @Email private String email;
    @NotBlank private String password;
}
