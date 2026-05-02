package com.complaintiq.escalation;
import com.complaintiq.escalation.enums.EscalationReason;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface EscalationRepository extends JpaRepository<Escalation, Long> {
    List<Escalation> findByComplaintId(Long complaintId);
    List<Escalation> findByComplaintTicketId(String ticketId);
    boolean existsByComplaintIdAndReason(Long complaintId, EscalationReason reason);
    Page<Escalation> findByEscalatedToId(Long agentId, Pageable pageable);
    @Query("SELECT COUNT(e) FROM Escalation e WHERE e.reason = 'SLA_BREACH'")
    long countSlaBreaches();
    @Query("SELECT COUNT(e) FROM Escalation e WHERE e.escalatedTo.id = :agentId AND e.reason = 'SLA_BREACH'")
    long countSlaBreachesByAgent(@Param("agentId") Long agentId);
}
