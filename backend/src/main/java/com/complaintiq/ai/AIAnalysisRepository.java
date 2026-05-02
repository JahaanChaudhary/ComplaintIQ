package com.complaintiq.ai;
import com.complaintiq.complaint.enums.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface AIAnalysisRepository extends JpaRepository<AIAnalysis, Long> {
    Optional<AIAnalysis> findByComplaintId(Long complaintId);
    Optional<AIAnalysis> findByComplaintTicketId(String ticketId);
    boolean existsByComplaintId(Long complaintId);
    @Query("SELECT AVG(a.confidenceScore) FROM AIAnalysis a")
    Double findAverageConfidenceScore();
}
