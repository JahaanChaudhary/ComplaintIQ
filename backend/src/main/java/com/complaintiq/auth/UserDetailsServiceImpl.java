package com.complaintiq.auth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Slf4j @Service @RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AppUserRepository appUserRepository;
    @Override @Transactional(readOnly=true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findActiveByEmail(email).orElseThrow(() -> {
            log.warn("User not found for email: {}", email);
            return new UsernameNotFoundException("User not found with email: " + email);
        });
        return User.builder().username(appUser.getEmail()).password(appUser.getPassword())
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name())))
            .accountExpired(false).accountLocked(false).credentialsExpired(false).disabled(!appUser.getIsActive()).build();
    }
}
