package com.complaintiq.customer;
import com.complaintiq.customer.enums.CustomerTier;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
    @Query("SELECT c FROM Customer c WHERE c.email = :email AND c.isActive = true")
    Optional<Customer> findActiveByEmail(@Param("email") String email);
    @Modifying
    @Query("UPDATE Customer c SET c.totalComplaints = c.totalComplaints + 1 WHERE c.id = :id")
    void incrementTotalComplaints(@Param("id") Long id);
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.tier = :tier")
    long countByTier(@Param("tier") CustomerTier tier);
}
