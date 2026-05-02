package com.complaintiq.agent.dto;
import com.complaintiq.agent.enums.AgentRole;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AgentCreateDTO {
    @NotBlank(message="Name is required") @Size(max=100) private String name;
    @NotBlank(message="Email is required") @Email private String email;
    @Pattern(regexp="^[+]?[0-9]{10,15}$",message="Invalid phone number format") private String phone;
    @NotNull(message="Department ID is required") private Long departmentId;
    @NotNull(message="Role is required") private AgentRole role;
    @Min(1) @Max(50) @Builder.Default private Integer maxLoad = 10;
    @NotBlank(message="Password is required") @Size(min=8) private String password;
}
