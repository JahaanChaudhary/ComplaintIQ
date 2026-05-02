package com.complaintiq.auth.dto;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponseDTO {
    private String accessToken; private String refreshToken;
    @Builder.Default private String tokenType = "Bearer";
    private Long expiresIn; private String email; private String name; private String role;
}
