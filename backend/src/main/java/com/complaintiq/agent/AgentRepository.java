package com.complaintiq.agent;
import com.complaintiq.agent.enums.AgentRole;
import com.complaintiq.department.Department;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    Optional<Agent> findByEmail(String email);
    boolean existsByEmail(String email);
    @Query("SELECT a FROM Agent a WHERE a.department = :department AND a.isAvailable = true AND a.currentLoad < a.maxLoad ORDER BY a.currentLoad ASC")
    List<Agent> findAvailableAgentsByDepartmentOrderByLoad(@Param("department") Department department);
    @Query("SELECT a FROM Agent a WHERE a.department = :department AND a.isAvailable = true AND a.currentLoad < a.maxLoad AND a.role IN ('SENIOR','TEAM_LEAD','MANAGER') ORDER BY a.currentLoad ASC")
    List<Agent> findSeniorAvailableAgentsByDepartment(@Param("department") Department department);
    @Query("SELECT a FROM Agent a WHERE a.department = :department AND a.role = :role AND a.isAvailable = true ORDER BY a.currentLoad ASC")
    List<Agent> findByDepartmentAndRole(@Param("department") Department department, @Param("role") AgentRole role);
    @Query("SELECT a FROM Agent a WHERE a.id = :headAgentId")
    Optional<Agent> findDepartmentHead(@Param("headAgentId") Long headAgentId);
    @Modifying
    @Query("UPDATE Agent a SET a.currentLoad = a.currentLoad + 1 WHERE a.id = :id")
    void incrementLoad(@Param("id") Long id);
    @Modifying
    @Query("UPDATE Agent a SET a.currentLoad = GREATEST(0, a.currentLoad - 1), a.totalResolved = a.totalResolved + 1 WHERE a.id = :id")
    void decrementLoadAndIncrementResolved(@Param("id") Long id);
    @Query("SELECT a FROM Agent a ORDER BY a.totalResolved DESC")
    List<Agent> findTopPerformers(Pageable pageable);
    @Query("SELECT COUNT(a) FROM Agent a WHERE a.isAvailable = true AND a.department.id = :deptId")
    long countAvailableByDepartment(@Param("deptId") Long deptId);
}
