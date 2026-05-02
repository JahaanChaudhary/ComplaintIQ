package com.complaintiq.auth;
import com.complaintiq.auth.dto.*;
import com.complaintiq.auth.enums.UserRole;
import com.complaintiq.customer.Customer;
import com.complaintiq.customer.CustomerRepository;
import com.complaintiq.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Slf4j @Service @RequiredArgsConstructor
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final CustomerRepository customerRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (appUserRepository.existsByEmail(request.getEmail().toLowerCase()))
            throw new DuplicateResourceException("User","email",request.getEmail());
        Customer customer = customerRepository.save(Customer.builder().name(request.getName()).email(request.getEmail().toLowerCase()).phone(request.getPhone()).build());
        AppUser appUser = appUserRepository.save(AppUser.builder().name(request.getName()).email(request.getEmail().toLowerCase()).password(passwordEncoder.encode(request.getPassword())).role(UserRole.CUSTOMER).customerId(customer.getId()).isActive(true).build());
        log.info("New customer registered: email={}", appUser.getEmail());
        return buildAuthResponse(appUser);
    }
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword()));
        AppUser appUser = appUserRepository.findActiveByEmail(request.getEmail().toLowerCase()).orElseThrow(() -> new ResourceNotFoundException("User","email",request.getEmail()));
        log.info("User logged in: email={} role={}", appUser.getEmail(), appUser.getRole());
        return buildAuthResponse(appUser);
    }
    @Transactional
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        AppUser appUser = appUserRepository.findById(refreshToken.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User","id",refreshToken.getUserId()));
        refreshTokenService.revokeToken(request.getRefreshToken());
        return buildAuthResponse(appUser);
    }
    @Transactional
    public void logout(Long userId) { refreshTokenService.revokeByUserId(userId); log.info("User logged out: userId={}", userId); }
    private AuthResponseDTO buildAuthResponse(AppUser user) {
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole(), user.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId(), user.getEmail());
        return AuthResponseDTO.builder().accessToken(accessToken).refreshToken(refreshToken.getToken()).tokenType("Bearer").expiresIn(jwtUtil.getAccessTokenExpiryMs()/1000).email(user.getEmail()).name(user.getName()).role(user.getRole().name()).build();
    }
}
