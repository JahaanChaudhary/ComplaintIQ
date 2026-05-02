package com.complaintiq.auth.dto;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterRequestDTO {
    @NotBlank @Size(max=100) private String name;
    @NotBlank @Email private String email;
    @NotBlank @Size(min=8) private String password;
    @Pattern(regexp="^[+]?[0-9]{10,15}$",message="Invalid phone number format") private String phone;
}
