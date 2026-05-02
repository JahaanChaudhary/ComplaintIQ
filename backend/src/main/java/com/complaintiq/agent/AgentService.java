package com.complaintiq.agent;
import com.complaintiq.agent.dto.*;
import com.complaintiq.agent.enums.AgentRole;
import com.complaintiq.auth.*;
import com.complaintiq.auth.enums.UserRole;
import com.complaintiq.common.PagedResponse;
import com.complaintiq.complaint.ComplaintService;
import com.complaintiq.complaint.dto.ComplaintSummaryDTO;
import com.complaintiq.department.DepartmentRepository;
import com.complaintiq.escalation.EscalationRepository;
import com.complaintiq.exception.*;
import com.complaintiq.resolution.ResolutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j @Service @RequiredArgsConstructor
public class AgentService {
    private final AgentRepository agentRepository;
    private final AppUserRepository appUserRepository;
    private final DepartmentRepository departmentRepository;
    private final ResolutionRepository resolutionRepository;
    private final EscalationRepository escalationRepository;
    private final ComplaintService complaintService;
    private final PasswordEncoder passwordEncoder;
    @Transactional
    public AgentResponseDTO createAgent(AgentCreateDTO request) {
        if (agentRepository.existsByEmail(request.getEmail().toLowerCase())) throw new DuplicateResourceException("Agent","email",request.getEmail());
        if (appUserRepository.existsByEmail(request.getEmail().toLowerCase())) throw new DuplicateResourceException("User","email",request.getEmail());
        var department = departmentRepository.findById(request.getDepartmentId()).orElseThrow(() -> new ResourceNotFoundException("Department","id",request.getDepartmentId()));
        Agent agent = Agent.builder().name(request.getName().trim()).email(request.getEmail().toLowerCase()).phone(request.getPhone()).department(department).role(request.getRole()).maxLoad(request.getMaxLoad() != null ? request.getMaxLoad() : 10).isAvailable(true).currentLoad(0).totalResolved(0).build();
        Agent savedAgent = agentRepository.save(agent);
        UserRole userRole = switch (request.getRole()) { case MANAGER -> UserRole.MANAGER; case TEAM_LEAD -> UserRole.TEAM_LEAD; default -> UserRole.AGENT; };
        AppUser appUser = AppUser.builder().name(request.getName().trim()).email(request.getEmail().toLowerCase()).password(passwordEncoder.encode(request.getPassword())).role(userRole).agentId(savedAgent.getId()).isActive(true).build();
        AppUser savedUser = appUserRepository.save(appUser);
        savedAgent.setUserId(savedUser.getId()); agentRepository.save(savedAgent);
        log.info("Agent created: name={} email={} role={}", savedAgent.getName(), savedAgent.getEmail(), savedAgent.getRole());
        return mapToResponseDTO(savedAgent);
    }
    @Transactional(readOnly=true)
    public PagedResponse<AgentResponseDTO> getAllAgents(int page, int size) {
        Page<Agent> agents = agentRepository.findAll(PageRequest.of(page, size, Sort.by("name").ascending()));
        return PagedResponse.from(agents, agents.getContent().stream().map(this::mapToResponseDTO).collect(Collectors.toList()));
    }
    @Transactional(readOnly=true)
    public AgentResponseDTO getById(Long id) { return mapToResponseDTO(agentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Agent","id",id))); }
    @Transactional(readOnly=true)
    public AgentDashboardDTO getDashboard(Long agentId) {
        Agent agent = agentRepository.findById(agentId).orElseThrow(() -> new ResourceNotFoundException("Agent","id",agentId));
        PagedResponse<ComplaintSummaryDTO> complaintsPage = complaintService.getComplaintsByAgent(agentId, 0, 20);
        long todayResolved = resolutionRepository.countByAgentId(agentId);
        long slaBreachCount = escalationRepository.countSlaBreachesByAgent(agentId);
        Double avgResolutionTime = resolutionRepository.findAvgResolutionHoursByAgent(agentId);
        return AgentDashboardDTO.builder().agentId(agent.getId()).agentName(agent.getName()).departmentName(agent.getDepartment().getName()).role(agent.getRole().name()).isAvailable(agent.getIsAvailable()).currentLoad(agent.getCurrentLoad()).maxLoad(agent.getMaxLoad()).todayResolved(todayResolved).avgResolutionTimeHours(avgResolutionTime != null ? avgResolutionTime : 0.0).slaBreachCount(slaBreachCount).totalResolved((long)agent.getTotalResolved()).assignedComplaints(complaintsPage.getContent()).build();
    }
    @Transactional
    public AgentResponseDTO toggleAvailability(Long agentId) {
        Agent agent = agentRepository.findById(agentId).orElseThrow(() -> new ResourceNotFoundException("Agent","id",agentId));
        agent.setIsAvailable(!agent.getIsAvailable()); agentRepository.save(agent);
        log.info("Agent availability toggled: agentId={} available={}", agentId, agent.getIsAvailable());
        return mapToResponseDTO(agent);
    }
    @Transactional(readOnly=true)
    public Agent getEntityByEmail(String email) { return agentRepository.findByEmail(email.toLowerCase()).orElseThrow(() -> new ResourceNotFoundException("Agent","email",email)); }
    @Transactional(readOnly=true)
    public List<AgentResponseDTO> getTopPerformers(int limit) { return agentRepository.findTopPerformers(PageRequest.of(0, limit)).stream().map(this::mapToResponseDTO).collect(Collectors.toList()); }
    public AgentResponseDTO mapToResponseDTO(Agent agent) {
        return AgentResponseDTO.builder().id(agent.getId()).name(agent.getName()).email(agent.getEmail()).phone(agent.getPhone()).role(agent.getRole()).departmentName(agent.getDepartment() != null ? agent.getDepartment().getName() : null).departmentId(agent.getDepartment() != null ? agent.getDepartment().getId() : null).isAvailable(agent.getIsAvailable()).currentLoad(agent.getCurrentLoad()).maxLoad(agent.getMaxLoad()).totalResolved(agent.getTotalResolved()).avgResolutionTimeHours(agent.getAvgResolutionTimeHours()).build();
    }
}
