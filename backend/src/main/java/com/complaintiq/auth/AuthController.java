package com.complaintiq.auth;
import com.complaintiq.auth.dto.*;
import com.complaintiq.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
@Tag(name="Authentication",description="Register, login, refresh token, logout")
public class AuthController {
    private final AuthService authService;
    private final AppUserRepository appUserRepository;
    private final JwtUtil jwtUtil;
    @Operation(summary="Register new customer")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Registration successful. Welcome to ComplaintIQ!", authService.register(request)));
    }
    @Operation(summary="Login")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful.", authService.login(request)));
    }
    @Operation(summary="Refresh access token")
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully.", authService.refreshToken(request)));
    }
    @Operation(summary="Logout")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Not authenticated.", "UNAUTHENTICATED"));
        }
        AppUser appUser = appUserRepository.findActiveByEmail(userDetails.getUsername()).orElseThrow();
        authService.logout(appUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully."));
    }
    @Operation(summary="Get current user profile")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Not authenticated.", "UNAUTHENTICATED"));
        }
        AppUser appUser = appUserRepository.findActiveByEmail(userDetails.getUsername()).orElseThrow();
        UserProfileDTO profile = UserProfileDTO.builder().id(appUser.getId()).name(appUser.getName()).email(appUser.getEmail()).role(appUser.getRole().name()).customerId(appUser.getCustomerId()).agentId(appUser.getAgentId()).build();
        return ResponseEntity.ok(ApiResponse.success("User profile fetched.", profile));
    }
}
