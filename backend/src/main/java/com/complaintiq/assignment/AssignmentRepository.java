package com.complaintiq.assignment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Optional<Assignment> findByComplaintId(Long complaintId);
    Optional<Assignment> findByComplaintTicketId(String ticketId);
    boolean existsByComplaintId(Long complaintId);
    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.agent.id = :agentId AND a.complaint.status IN ('OPEN','ASSIGNED','IN_PROGRESS')")
    long countActiveByAgentId(@Param("agentId") Long agentId);
    @Query("SELECT a FROM Assignment a WHERE a.agent.id = :agentId ORDER BY a.assignedAt DESC")
    List<Assignment> findByAgentIdOrderByAssignedAtDesc(@Param("agentId") Long agentId);
}
