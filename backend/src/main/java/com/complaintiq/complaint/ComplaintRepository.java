package com.complaintiq.complaint;
import com.complaintiq.complaint.enums.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.*;
@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long>, JpaSpecificationExecutor<Complaint> {
    Optional<Complaint> findByTicketId(String ticketId);
    boolean existsByTicketId(String ticketId);
    Page<Complaint> findByCustomerId(Long customerId, Pageable pageable);
    @Query("SELECT c FROM Complaint c WHERE c.status IN ('OPEN','ASSIGNED','IN_PROGRESS') AND c.slaDeadline IS NOT NULL")
    List<Complaint> findAllActiveForSlaCheck();
    @Query("SELECT c FROM Complaint c WHERE c.status IN ('OPEN','ASSIGNED','IN_PROGRESS') AND c.slaDeadline IS NOT NULL AND c.slaDeadline < :now")
    List<Complaint> findSlaBreachedComplaints(@Param("now") LocalDateTime now);
    @Query("SELECT c.status, COUNT(c) FROM Complaint c GROUP BY c.status")
    List<Object[]> countByStatus();
    @Query("SELECT a.category, COUNT(a) FROM AIAnalysis a GROUP BY a.category")
    List<Object[]> countByCategory();
    @Query("SELECT c.urgency, COUNT(c) FROM Complaint c GROUP BY c.urgency")
    List<Object[]> countByUrgency();
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.status = 'RESOLVED' AND c.resolvedAt >= :startOfDay")
    long countResolvedToday(@Param("startOfDay") LocalDateTime startOfDay);
    @Query(value = "SELECT DATE(created_at) as day, COUNT(*) FROM complaints WHERE created_at >= :from GROUP BY day ORDER BY day ASC", nativeQuery = true)
    List<Object[]> findDailyTrend(@Param("from") LocalDateTime from);

    @Query(value = "SELECT EXTRACT(HOUR FROM created_at)::int, COUNT(*) FROM complaints GROUP BY EXTRACT(HOUR FROM created_at) ORDER BY EXTRACT(HOUR FROM created_at)", nativeQuery = true)
    List<Object[]> findHourlyVolume();

    @Query(value = "SELECT MAX(CAST(SPLIT_PART(ticket_id, '-', 3) AS INTEGER)) FROM complaints WHERE ticket_id LIKE 'CIQ-%'", nativeQuery = true)
    Optional<Long> findMaxTicketSequence();
    @Modifying
    @Query("UPDATE Complaint c SET c.status = :status WHERE c.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") ComplaintStatus status);
    long countByStatus(ComplaintStatus status);
    long countByUrgencyAndStatus(UrgencyLevel urgency, ComplaintStatus status);
    @Query("SELECT c FROM Complaint c JOIN Assignment a ON a.complaint = c WHERE a.agent.id = :agentId AND c.status IN ('OPEN','ASSIGNED','IN_PROGRESS')")
    Page<Complaint> findActiveComplaintsByAgentId(@Param("agentId") Long agentId, Pageable pageable);
}
