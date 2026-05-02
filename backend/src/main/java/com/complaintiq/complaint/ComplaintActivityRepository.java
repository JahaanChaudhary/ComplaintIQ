package com.complaintiq.complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ComplaintActivityRepository extends JpaRepository<ComplaintActivity, Long> {
    List<ComplaintActivity> findByComplaintIdOrderByPerformedAtAsc(Long complaintId);
    void deleteByComplaintId(Long complaintId);
}
