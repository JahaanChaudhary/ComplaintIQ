package com.complaintiq.auth;
import com.complaintiq.auth.enums.UserRole;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
    List<AppUser> findByRole(UserRole role);
    @Query("SELECT u FROM AppUser u WHERE u.email = :email AND u.isActive = true")
    Optional<AppUser> findActiveByEmail(@Param("email") String email);
    @Query("SELECT u FROM AppUser u WHERE u.agentId = :agentId")
    Optional<AppUser> findByAgentId(@Param("agentId") Long agentId);
    @Query("SELECT u FROM AppUser u WHERE u.customerId = :customerId")
    Optional<AppUser> findByCustomerId(@Param("customerId") Long customerId);
}
