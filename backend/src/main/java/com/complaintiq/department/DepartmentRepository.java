package com.complaintiq.department;
import com.complaintiq.complaint.enums.ComplaintCategory;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByCategory(ComplaintCategory category);
    Optional<Department> findByName(String name);
    boolean existsByName(String name);
    boolean existsByCategory(ComplaintCategory category);
    @Query("SELECT d FROM Department d WHERE d.headAgentId = :agentId")
    Optional<Department> findByHeadAgentId(@Param("agentId") Long agentId);
}
