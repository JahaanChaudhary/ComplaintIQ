package com.complaintiq.assignment;
import com.complaintiq.agent.Agent;
import com.complaintiq.agent.AgentRepository;
import com.complaintiq.agent.enums.AgentRole;
import com.complaintiq.ai.AIAnalysis;
import com.complaintiq.ai.enums.ComplaintIntent;
import com.complaintiq.assignment.dto.*;
import com.complaintiq.assignment.enums.AssignmentStatus;
import com.complaintiq.common.MdcUtil;
import com.complaintiq.complaint.*;
import com.complaintiq.complaint.enums.ComplaintStatus;
import com.complaintiq.complaint.enums.UrgencyLevel;
import com.complaintiq.customer.enums.CustomerTier;
import com.complaintiq.department.*;
import com.complaintiq.exception.*;
import com.complaintiq.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
@Slf4j @Service @RequiredArgsConstructor
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final AgentRepository agentRepository;
    private final DepartmentRepository departmentRepository;
    private final ComplaintRepository complaintRepository;
    private final ComplaintActivityRepository activityRepository;
    private final NotificationService notificationService;
    @Transactional
    public Assignment assignComplaint(Complaint complaint, AIAnalysis aiAnalysis) {
        String ticketId = complaint.getTicketId(); MdcUtil.setTicketId(ticketId);
        try {
            Department department = departmentRepository.findByCategory(aiAnalysis.getCategory()).orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElseThrow(() -> new AssignmentException(ticketId, "No departments exist in system")));
            Agent selectedAgent = selectAgent(complaint, aiAnalysis, department, ticketId);
            Assignment assignment = Assignment.builder().complaint(complaint).agent(selectedAgent).department(department).status(AssignmentStatus.ASSIGNED).assignedAt(LocalDateTime.now()).build();
            Assignment saved = assignmentRepository.save(assignment);
            agentRepository.incrementLoad(selectedAgent.getId());
            complaint.setStatus(ComplaintStatus.ASSIGNED); complaintRepository.save(complaint);
            logActivity(complaint.getId(), "Complaint assigned to agent: " + selectedAgent.getName(), "SYSTEM", "Department: " + department.getName());
            log.info("Complaint assigned: ticketId={} agentName={} department={}", ticketId, selectedAgent.getName(), department.getName());
            notificationService.notifyAgentOfAssignment(saved, complaint, aiAnalysis);
            return saved;
        } finally { MdcUtil.clear(); }
    }
    private Agent selectAgent(Complaint complaint, AIAnalysis aiAnalysis, Department department, String ticketId) {
        boolean isCritical = aiAnalysis.getUrgency() == UrgencyLevel.CRITICAL || aiAnalysis.getUrgency() == UrgencyLevel.HIGH;
        boolean isLegalThreat = aiAnalysis.getIntent() == ComplaintIntent.LEGAL_THREAT;
        boolean isVip = complaint.getCustomer().getTier() == CustomerTier.VIP || complaint.getCustomer().getTier() == CustomerTier.PREMIUM;
        if (isLegalThreat || isCritical) {
            Optional<Agent> senior = agentRepository.findSeniorAvailableAgentsByDepartment(department).stream().findFirst();
            if (senior.isPresent()) return senior.get();
        }
        if (isVip) {
            Optional<Agent> senior = agentRepository.findSeniorAvailableAgentsByDepartment(department).stream().findFirst();
            if (senior.isPresent()) return senior.get();
        }
        List<Agent> available = agentRepository.findAvailableAgentsByDepartmentOrderByLoad(department);
        if (!available.isEmpty()) return available.get(0);
        if (department.getHeadAgentId() != null) {
            Optional<Agent> head = agentRepository.findDepartmentHead(department.getHeadAgentId());
            if (head.isPresent()) return head.get();
        }
        return agentRepository.findAll().stream().filter(a -> Boolean.TRUE.equals(a.getIsAvailable())).findFirst().orElseThrow(() -> new AssignmentException(ticketId, "No agents available in the entire system"));
    }
    @Transactional
    public AssignmentResponseDTO reassignComplaint(String ticketId, AssignmentReassignDTO request, String performedBy) {
        MdcUtil.setTicketId(ticketId);
        try {
            Assignment assignment = assignmentRepository.findByComplaintTicketId(ticketId).orElseThrow(() -> new ResourceNotFoundException("Assignment","ticketId",ticketId));
            Agent newAgent = agentRepository.findById(request.getNewAgentId()).orElseThrow(() -> new ResourceNotFoundException("Agent","id",request.getNewAgentId()));
            Agent previousAgent = assignment.getAgent();
            previousAgent.setCurrentLoad(Math.max(0, previousAgent.getCurrentLoad() - 1));
            assignment.setAgent(newAgent); assignment.setStatus(AssignmentStatus.REASSIGNED); assignment.setReassignedAt(LocalDateTime.now()); assignment.setReassignReason(request.getReason());
            Assignment saved = assignmentRepository.save(assignment);
            agentRepository.incrementLoad(newAgent.getId());
            logActivity(assignment.getComplaint().getId(), "Complaint reassigned from " + previousAgent.getName() + " to " + newAgent.getName(), performedBy, "Reason: " + request.getReason());
            log.info("Complaint reassigned: ticketId={} from={} to={}", ticketId, previousAgent.getName(), newAgent.getName());
            notificationService.notifyAgentOfReassignment(saved, assignment.getComplaint());
            return mapToResponseDTO(saved);
        } finally { MdcUtil.clear(); }
    }
    @Transactional(readOnly=true)
    public AssignmentResponseDTO getByTicketId(String ticketId) {
        return mapToResponseDTO(assignmentRepository.findByComplaintTicketId(ticketId).orElseThrow(() -> new ResourceNotFoundException("Assignment","ticketId",ticketId)));
    }
    @Transactional
    public void releaseAgentLoad(String ticketId) {
        assignmentRepository.findByComplaintTicketId(ticketId).ifPresent(a -> { agentRepository.decrementLoadAndIncrementResolved(a.getAgent().getId()); log.info("Agent load released: ticketId={} agent={}", ticketId, a.getAgent().getName()); });
    }
    private AssignmentResponseDTO mapToResponseDTO(Assignment a) {
        return AssignmentResponseDTO.builder().id(a.getId()).ticketId(a.getComplaint().getTicketId()).agentId(a.getAgent().getId()).agentName(a.getAgent().getName()).departmentId(a.getDepartment().getId()).departmentName(a.getDepartment().getName()).status(a.getStatus()).assignedAt(a.getAssignedAt()).reassignedAt(a.getReassignedAt()).reassignReason(a.getReassignReason()).build();
    }
    private void logActivity(Long complaintId, String action, String performedBy, String notes) {
        activityRepository.save(ComplaintActivity.builder().complaintId(complaintId).action(action).performedBy(performedBy).notes(notes).performedAt(LocalDateTime.now()).build());
    }
}
