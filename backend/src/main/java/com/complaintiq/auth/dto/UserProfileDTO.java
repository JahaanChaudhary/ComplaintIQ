package com.complaintiq.auth.dto;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfileDTO {
    private Long id; private String name; private String email;
    private String role; private Long customerId; private Long agentId;
}
