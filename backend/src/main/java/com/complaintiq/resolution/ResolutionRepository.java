package com.complaintiq.resolution;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface ResolutionRepository extends JpaRepository<Resolution, Long> {
    Optional<Resolution> findByComplaintId(Long complaintId);
    Optional<Resolution> findByComplaintTicketId(String ticketId);
    boolean existsByComplaintId(Long complaintId);
    @Query("SELECT AVG(r.satisfactionScore) FROM Resolution r WHERE r.satisfactionScore IS NOT NULL")
    Double findAverageSatisfactionScore();
    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (r.resolved_at - c.created_at)) / 3600.0) FROM resolutions r JOIN complaints c ON r.complaint_id = c.id WHERE r.resolved_by_id = :agentId", nativeQuery = true)
    Double findAvgResolutionHoursByAgent(@Param("agentId") Long agentId);
    @Query("SELECT r FROM Resolution r WHERE r.satisfactionScore IS NOT NULL AND r.satisfactionScore <= 2 AND r.complaint.status = 'RESOLVED'")
    List<Resolution> findLowSatisfactionResolutions();
    @Query("SELECT COUNT(r) FROM Resolution r WHERE r.resolvedBy.id = :agentId")
    long countByAgentId(@Param("agentId") Long agentId);
}
