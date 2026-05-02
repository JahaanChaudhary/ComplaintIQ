package com.complaintiq.sla;
import com.complaintiq.complaint.enums.UrgencyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface SLAConfigRepository extends JpaRepository<SLAConfig, Long> {
    Optional<SLAConfig> findByUrgencyLevel(UrgencyLevel urgencyLevel);
    boolean existsByUrgencyLevel(UrgencyLevel urgencyLevel);
}
