package com.complaintiq.complaint;
import com.complaintiq.ai.*;
import com.complaintiq.assignment.*;
import com.complaintiq.assignment.dto.*;
import com.complaintiq.common.*;
import com.complaintiq.complaint.dto.*;
import com.complaintiq.complaint.enums.*;
import com.complaintiq.customer.Customer;
import com.complaintiq.customer.CustomerRepository;
import com.complaintiq.escalation.*;
import com.complaintiq.escalation.dto.*;
import com.complaintiq.exception.*;
import com.complaintiq.notification.NotificationService;
import com.complaintiq.resolution.*;
import com.complaintiq.resolution.dto.*;
import com.complaintiq.sla.SLAService;
import com.complaintiq.websocket.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j @Service @RequiredArgsConstructor
public class ComplaintService {
    private final ComplaintRepository complaintRepository;
    private final CustomerRepository customerRepository;
    private final ComplaintActivityRepository activityRepository;
    private final AssignmentRepository assignmentRepository;
    private final ResolutionRepository resolutionRepository;
    private final EscalationRepository escalationRepository;
    private final AIAnalysisService aiAnalysisService;
    private final AssignmentService assignmentService;
    private final SLAService slaService;
    private final NotificationService notificationService;
    private final WebSocketController webSocketController;
    private final TicketIdGenerator ticketIdGenerator;
    private final com.complaintiq.agent.AgentRepository agentRepository;

    @Transactional
    public ComplaintResponseDTO createComplaint(ComplaintRequestDTO request) {
        Customer customer = customerRepository.findActiveByEmail(request.getCustomerEmail().toLowerCase()).orElseThrow(() -> new ResourceNotFoundException("Customer","email",request.getCustomerEmail()));
        String ticketId = ticketIdGenerator.generate();
        MdcUtil.setTicketId(ticketId);
        try {
            Complaint complaint = Complaint.builder().ticketId(ticketId).customer(customer).title(request.getTitle().trim()).description(request.getDescription().trim()).orderId(request.getOrderId()).channel(request.getChannel()).status(ComplaintStatus.OPEN).imageUrl(request.getImageUrl()).build();
            Complaint saved = complaintRepository.save(complaint);
            log.info("Complaint created: ticketId={} customerEmail={} channel={}", ticketId, customer.getEmail(), request.getChannel());
            customerRepository.incrementTotalComplaints(customer.getId());
            logActivity(saved.getId(), "Complaint submitted", customer.getEmail(), "Channel: " + request.getChannel().name());
            AIAnalysis aiAnalysis = aiAnalysisService.analyzeComplaint(saved, customer);
            saved.setUrgency(aiAnalysis.getUrgency()); complaintRepository.save(saved);
            slaService.calculateAndSetDeadline(saved, aiAnalysis.getUrgency());
            saved = complaintRepository.findById(saved.getId()).orElse(saved);
            Assignment assignment = assignmentService.assignComplaint(saved, aiAnalysis);
            notificationService.notifyCustomerOfComplaintReceived(saved);
            webSocketController.broadcastStatusUpdate(ComplaintStatusUpdate.builder().ticketId(ticketId).newStatus(ComplaintStatus.ASSIGNED.name()).message("Your complaint has been received and assigned.").timestamp(LocalDateTime.now()).slaStatus("ON_TIME").build());
            log.info("Complaint fully processed: ticketId={} urgency={} category={}", ticketId, aiAnalysis.getUrgency(), aiAnalysis.getCategory());
            return buildFullResponse(saved, aiAnalysis, assignment);
        } finally { MdcUtil.clear(); }
    }

    @Transactional(readOnly=true)
    public ComplaintResponseDTO getByTicketId(String ticketId) {
        Complaint complaint = complaintRepository.findByTicketId(ticketId).orElseThrow(() -> new ResourceNotFoundException("Complaint","ticketId",ticketId));
        Optional<AIAnalysis> ai = aiAnalysisService.getRepository().findByComplaintId(complaint.getId());
        Optional<Assignment> assign = assignmentRepository.findByComplaintId(complaint.getId());
        return buildFullResponse(complaint, ai.orElse(null), assign.orElse(null));
    }

    @Transactional(readOnly=true)
    public ComplaintResponseDTO getByTicketIdForUser(String ticketId, String userEmail, java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities) {
        Complaint complaint = complaintRepository.findByTicketId(ticketId).orElseThrow(() -> new ResourceNotFoundException("Complaint","ticketId",ticketId));
        boolean isStaff = authorities.stream().anyMatch(a -> {
            String role = a.getAuthority();
            return role.equals("ROLE_AGENT") || role.equals("ROLE_TEAM_LEAD") || role.equals("ROLE_MANAGER") || role.equals("ROLE_ADMIN");
        });
        if (!isStaff) {
            String complaintOwnerEmail = complaint.getCustomer() != null ? complaint.getCustomer().getEmail() : null;
            if (complaintOwnerEmail == null || !complaintOwnerEmail.equalsIgnoreCase(userEmail)) {
                throw new org.springframework.security.access.AccessDeniedException("You do not have access to this complaint.");
            }
        }
        Optional<AIAnalysis> ai = aiAnalysisService.getRepository().findByComplaintId(complaint.getId());
        Optional<Assignment> assign = assignmentRepository.findByComplaintId(complaint.getId());
        return buildFullResponse(complaint, ai.orElse(null), assign.orElse(null));
    }

    @Transactional(readOnly=true)
    public PagedResponse<ComplaintSummaryDTO> getAllComplaints(ComplaintFilterDTO filter) {
        Sort sort = filter.getSortDir().equalsIgnoreCase("asc") ? Sort.by(filter.getSortBy()).ascending() : Sort.by(filter.getSortBy()).descending();
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        Page<Complaint> page = complaintRepository.findAll(ComplaintSpecification.withFilters(filter.getStatus(), filter.getUrgency(), filter.getCategory(), filter.getAgentId(), filter.getCustomerId(), filter.getDateFrom(), filter.getDateTo(), filter.getKeyword()), pageable);
        return PagedResponse.from(page, page.getContent().stream().map(this::buildSummaryDTO).collect(Collectors.toList()));
    }

    @Transactional(readOnly=true)
    public PagedResponse<ComplaintSummaryDTO> getMyComplaints(String customerEmail, int page, int size) {
        Customer customer = customerRepository.findActiveByEmail(customerEmail.toLowerCase()).orElseThrow(() -> new ResourceNotFoundException("Customer","email",customerEmail));
        Page<Complaint> complaints = complaintRepository.findByCustomerId(customer.getId(), PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PagedResponse.from(complaints, complaints.getContent().stream().map(this::buildSummaryDTO).collect(Collectors.toList()));
    }

    @Transactional(readOnly=true)
    public ComplaintResponseDTO trackComplaint(String ticketId) {
        Complaint complaint = complaintRepository.findByTicketId(ticketId).orElseThrow(() -> new ResourceNotFoundException("Complaint","ticketId",ticketId));
        Optional<AIAnalysis> ai = aiAnalysisService.getRepository().findByComplaintId(complaint.getId());
        Optional<Assignment> assign = assignmentRepository.findByComplaintId(complaint.getId());
        return buildPublicTrackingResponse(complaint, ai.orElse(null), assign.orElse(null));
    }

    @Transactional
    public ComplaintResponseDTO updateStatus(String ticketId, StatusUpdateDTO request, String performedBy) {
        MdcUtil.setTicketId(ticketId);
        try {
            Complaint complaint = complaintRepository.findByTicketId(ticketId).orElseThrow(() -> new ResourceNotFoundException("Complaint","ticketId",ticketId));
            ComplaintStatus oldStatus = complaint.getStatus(); ComplaintStatus newStatus = request.getStatus();
            // RESOLVED requires a Resolution record — force the caller to use the /resolve endpoint.
            if (newStatus == ComplaintStatus.RESOLVED) {
                throw new ComplaintStateException("To mark as RESOLVED, use the dedicated /resolve endpoint which requires a resolution note and type.");
            }
            validateStatusTransition(ticketId, oldStatus, newStatus);
            complaint.setStatus(newStatus);
            complaintRepository.save(complaint);
            logActivity(complaint.getId(), "Status changed: " + oldStatus + " to " + newStatus, performedBy, request.getNote());
            log.info("Status updated: ticketId={} {} to {} by={}", ticketId, oldStatus, newStatus, performedBy);
            webSocketController.broadcastStatusUpdate(ComplaintStatusUpdate.builder().ticketId(ticketId).newStatus(newStatus.name()).message("Complaint status updated to " + newStatus.name()).timestamp(LocalDateTime.now()).slaStatus(slaService.computeSlaStatus(complaint).name()).build());
            Optional<AIAnalysis> ai = aiAnalysisService.getRepository().findByComplaintId(complaint.getId());
            Optional<Assignment> assign = assignmentRepository.findByComplaintId(complaint.getId());
            return buildFullResponse(complaint, ai.orElse(null), assign.orElse(null));
        } finally { MdcUtil.clear(); }
    }

    @Transactional
    public ResolutionResponseDTO resolveComplaint(String ticketId, ResolutionRequestDTO request, String agentEmail) {
        MdcUtil.setTicketId(ticketId);
        try {
            Complaint complaint = complaintRepository.findByTicketId(ticketId).orElseThrow(() -> new ResourceNotFoundException("Complaint","ticketId",ticketId));
            if (complaint.getStatus() == ComplaintStatus.RESOLVED || complaint.getStatus() == ComplaintStatus.CLOSED) throw new ComplaintStateException("Complaint " + ticketId + " is already " + complaint.getStatus());
            if (resolutionRepository.existsByComplaintId(complaint.getId())) throw new DuplicateResourceException("Resolution","complaintId",complaint.getId());
            Assignment assignment = assignmentRepository.findByComplaintId(complaint.getId()).orElseThrow(() -> new ResourceNotFoundException("Assignment","complaintId",complaint.getId()));
            complaint.setStatus(ComplaintStatus.RESOLVED); complaint.setResolvedAt(LocalDateTime.now()); complaintRepository.save(complaint);
            Resolution resolution = Resolution.builder().complaint(complaint).resolvedBy(assignment.getAgent()).resolutionNote(request.getResolutionNote()).resolutionType(request.getResolutionType()).resolvedAt(LocalDateTime.now()).build();
            Resolution saved = resolutionRepository.save(resolution);
            assignmentService.releaseAgentLoad(ticketId);
            logActivity(complaint.getId(), "Complaint resolved", agentEmail, "Type: " + request.getResolutionType().name());
            log.info("Complaint resolved: ticketId={} agent={} type={}", ticketId, assignment.getAgent().getName(), request.getResolutionType());
            webSocketController.broadcastStatusUpdate(ComplaintStatusUpdate.builder().ticketId(ticketId).newStatus(ComplaintStatus.RESOLVED.name()).message("Your complaint has been resolved.").timestamp(LocalDateTime.now()).slaStatus("ON_TIME").build());
            notificationService.notifyCustomerOfResolution(complaint, request.getResolutionNote(), request.getResolutionType().name());
            notificationService.sendFeedbackRequest(complaint);
            return ResolutionResponseDTO.builder().id(saved.getId()).ticketId(ticketId).resolutionNote(saved.getResolutionNote()).resolutionType(saved.getResolutionType()).resolvedByName(assignment.getAgent().getName()).resolvedAt(saved.getResolvedAt()).build();
        } finally { MdcUtil.clear(); }
    }

    @Transactional
    public ComplaintResponseDTO submitFeedback(String ticketId, FeedbackRequestDTO request, String customerEmail) {
        MdcUtil.setTicketId(ticketId);
        try {
            Complaint complaint = complaintRepository.findByTicketId(ticketId).orElseThrow(() -> new ResourceNotFoundException("Complaint","ticketId",ticketId));
            if (!complaint.getCustomer().getEmail().equalsIgnoreCase(customerEmail)) throw new ForbiddenException("You can only submit feedback for your own complaints.");
            if (complaint.getStatus() != ComplaintStatus.RESOLVED) throw new ComplaintStateException("Feedback can only be submitted for resolved complaints.");
            Resolution resolution = resolutionRepository.findByComplaintId(complaint.getId()).orElseThrow(() -> new ResourceNotFoundException("Resolution","complaintId",complaint.getId()));
            resolution.setSatisfactionScore(request.getSatisfactionScore()); resolution.setFeedbackComment(request.getFeedbackComment());
            resolutionRepository.save(resolution);
            logActivity(complaint.getId(), "Feedback submitted: score=" + request.getSatisfactionScore(), customerEmail, request.getFeedbackComment());
            log.info("Feedback submitted: ticketId={} score={}", ticketId, request.getSatisfactionScore());
            if (request.getSatisfactionScore() <= 2) reopenComplaint(complaint, resolution, customerEmail);
            Optional<AIAnalysis> ai = aiAnalysisService.getRepository().findByComplaintId(complaint.getId());
            Optional<Assignment> assign = assignmentRepository.findByComplaintId(complaint.getId());
            return buildFullResponse(complaint, ai.orElse(null), assign.orElse(null));
        } finally { MdcUtil.clear(); }
    }

    @Transactional
    public EscalationResponseDTO escalateComplaint(String ticketId, EscalationRequestDTO request, String performedBy) {
        MdcUtil.setTicketId(ticketId);
        try {
            Complaint complaint = complaintRepository.findByTicketId(ticketId).orElseThrow(() -> new ResourceNotFoundException("Complaint","ticketId",ticketId));
            if (complaint.getStatus() == ComplaintStatus.RESOLVED || complaint.getStatus() == ComplaintStatus.CLOSED) throw new ComplaintStateException("Cannot escalate a " + complaint.getStatus() + " complaint.");
            Assignment assignment = assignmentRepository.findByComplaintId(complaint.getId()).orElseThrow(() -> new ResourceNotFoundException("Assignment","complaintId",complaint.getId()));
            com.complaintiq.agent.Agent escalationTarget = agentRepository.findAll().stream().filter(a -> a.getRole() == com.complaintiq.agent.enums.AgentRole.MANAGER && Boolean.TRUE.equals(a.getIsAvailable())).findFirst().orElseThrow(() -> new ResourceNotFoundException("No available manager found for escalation"));
            complaint.setStatus(ComplaintStatus.ESCALATED); complaintRepository.save(complaint);
            Escalation escalation = Escalation.builder().complaint(complaint).escalatedFrom(assignment.getAgent()).escalatedTo(escalationTarget).reason(request.getReason()).notes(request.getNotes()).escalatedAt(LocalDateTime.now()).build();
            Escalation saved = escalationRepository.save(escalation);
            assignment.setAgent(escalationTarget); assignment.setReassignedAt(LocalDateTime.now()); assignment.setReassignReason("Manual escalation: " + request.getReason().name());
            assignmentRepository.save(assignment);
            logActivity(complaint.getId(), "Complaint manually escalated: " + request.getReason().name(), performedBy, "Escalated to: " + escalationTarget.getName());
            log.info("Manual escalation: ticketId={} reason={} escalatedTo={}", ticketId, request.getReason(), escalationTarget.getName());
            notificationService.notifyAgentOfEscalation(escalationTarget, complaint, 0);
            notificationService.notifyCustomerOfEscalation(complaint.getCustomer(), complaint);
            webSocketController.broadcastStatusUpdate(ComplaintStatusUpdate.builder().ticketId(ticketId).newStatus(ComplaintStatus.ESCALATED.name()).message("Your complaint has been escalated for priority resolution.").timestamp(LocalDateTime.now()).slaStatus(slaService.computeSlaStatus(complaint).name()).build());
            return EscalationResponseDTO.builder().id(saved.getId()).ticketId(ticketId).escalatedFromName(saved.getEscalatedFrom() != null ? saved.getEscalatedFrom().getName() : "UNASSIGNED").escalatedToName(escalationTarget.getName()).reason(saved.getReason()).notes(saved.getNotes()).escalatedAt(saved.getEscalatedAt()).build();
        } finally { MdcUtil.clear(); }
    }

    @Transactional
    public AssignmentResponseDTO reassignComplaint(String ticketId, AssignmentReassignDTO request, String performedBy) {
        complaintRepository.findByTicketId(ticketId).orElseThrow(() -> new ResourceNotFoundException("Complaint","ticketId",ticketId));
        return assignmentService.reassignComplaint(ticketId, request, performedBy);
    }

    @Transactional(readOnly=true)
    public PagedResponse<ComplaintSummaryDTO> getComplaintsByAgent(Long agentId, int page, int size) {
        Page<Complaint> complaints = complaintRepository.findActiveComplaintsByAgentId(agentId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PagedResponse.from(complaints, complaints.getContent().stream().map(this::buildSummaryDTO).collect(Collectors.toList()));
    }

    @Transactional
    private void reopenComplaint(Complaint complaint, Resolution resolution, String customerEmail) {
        int priorScore = resolution.getSatisfactionScore();
        // Delete the old Resolution so the agent can create a fresh one when they re-resolve.
        // Capture the score first (before deletion) for logging/notifications.
        resolutionRepository.delete(resolution);
        complaint.setStatus(ComplaintStatus.OPEN); complaint.setResolvedAt(null); complaintRepository.save(complaint);
        // Increment the agent's load back since they now own an open complaint again
        assignmentRepository.findByComplaintId(complaint.getId()).ifPresent(a -> {
            a.getAgent().setCurrentLoad(a.getAgent().getCurrentLoad() + 1);
            agentRepository.save(a.getAgent());
        });
        logActivity(complaint.getId(), "Complaint auto-reopened due to low satisfaction score: " + priorScore + "/5", "SYSTEM", "Customer: " + customerEmail);
        log.warn("Complaint auto-reopened: ticketId={} score={}", complaint.getTicketId(), priorScore);
        assignmentRepository.findByComplaintId(complaint.getId()).ifPresent(a -> notificationService.notifyAgentOfReopening(a.getAgent(), complaint, priorScore));
        webSocketController.broadcastStatusUpdate(ComplaintStatusUpdate.builder().ticketId(complaint.getTicketId()).newStatus(ComplaintStatus.OPEN.name()).message("Complaint reopened due to low satisfaction score.").timestamp(LocalDateTime.now()).slaStatus(slaService.computeSlaStatus(complaint).name()).build());
    }

    private void validateStatusTransition(String ticketId, ComplaintStatus current, ComplaintStatus next) {
        if (current == ComplaintStatus.CLOSED) throw new ComplaintStateException("Complaint " + ticketId + " is CLOSED and cannot be modified.");
        if (current == ComplaintStatus.RESOLVED && next == ComplaintStatus.RESOLVED) throw new ComplaintStateException("Complaint " + ticketId + " is already RESOLVED.");
    }

    public ComplaintResponseDTO buildFullResponse(Complaint complaint, AIAnalysis aiAnalysis, Assignment assignment) {
        List<ComplaintActivity> activities = activityRepository.findByComplaintIdOrderByPerformedAtAsc(complaint.getId());
        List<ComplaintResponseDTO.ActivityInfo> timeline = activities.stream().map(a -> ComplaintResponseDTO.ActivityInfo.builder().action(a.getAction()).performedBy(a.getPerformedBy()).notes(a.getNotes()).performedAt(a.getPerformedAt()).build()).collect(Collectors.toList());
        SlaStatus slaStatus = slaService.computeSlaStatus(complaint);
        ComplaintResponseDTO.CustomerInfo customerInfo = ComplaintResponseDTO.CustomerInfo.builder().id(complaint.getCustomer().getId()).name(complaint.getCustomer().getName()).email(complaint.getCustomer().getEmail()).tier(complaint.getCustomer().getTier().name()).build();
        ComplaintResponseDTO.AIAnalysisInfo aiInfo = null;
        if (aiAnalysis != null) aiInfo = ComplaintResponseDTO.AIAnalysisInfo.builder().urgency(aiAnalysis.getUrgency().name()).category(aiAnalysis.getCategory().name()).sentiment(aiAnalysis.getSentiment().name()).intent(aiAnalysis.getIntent().name()).summary(aiAnalysis.getSummary()).suggestedAction(aiAnalysis.getSuggestedAction()).confidenceScore(aiAnalysis.getConfidenceScore()).build();
        ComplaintResponseDTO.AssignmentInfo assignmentInfo = null;
        if (assignment != null) assignmentInfo = ComplaintResponseDTO.AssignmentInfo.builder().agentId(assignment.getAgent().getId()).agentName(assignment.getAgent().getName()).departmentName(assignment.getDepartment().getName()).assignedAt(assignment.getAssignedAt()).status(assignment.getStatus().name()).build();
        return ComplaintResponseDTO.builder().id(complaint.getId()).ticketId(complaint.getTicketId()).title(complaint.getTitle()).description(complaint.getDescription()).orderId(complaint.getOrderId()).status(complaint.getStatus()).channel(complaint.getChannel()).imageUrl(complaint.getImageUrl()).customer(customerInfo).aiAnalysis(aiInfo).assignment(assignmentInfo).slaDeadline(complaint.getSlaDeadline()).slaStatus(slaStatus).timeline(timeline).createdAt(complaint.getCreatedAt()).updatedAt(complaint.getUpdatedAt()).resolvedAt(complaint.getResolvedAt()).build();
    }

    public ComplaintSummaryDTO buildSummaryDTO(Complaint complaint) {
        SlaStatus slaStatus = slaService.computeSlaStatus(complaint);
        String agentName = null; String deptName = null;
        Optional<Assignment> assignment = assignmentRepository.findByComplaintId(complaint.getId());
        if (assignment.isPresent()) { agentName = assignment.get().getAgent().getName(); deptName = assignment.get().getDepartment().getName(); }
        return ComplaintSummaryDTO.builder().id(complaint.getId()).ticketId(complaint.getTicketId()).title(complaint.getTitle()).status(complaint.getStatus()).urgency(complaint.getUrgency()).customerName(complaint.getCustomer().getName()).customerEmail(complaint.getCustomer().getEmail()).customerTier(complaint.getCustomer().getTier().name()).assignedAgentName(agentName).departmentName(deptName).slaStatus(slaStatus).slaDeadline(complaint.getSlaDeadline()).createdAt(complaint.getCreatedAt()).build();
    }

    private ComplaintResponseDTO buildPublicTrackingResponse(Complaint complaint, AIAnalysis aiAnalysis, Assignment assignment) {
        SlaStatus slaStatus = slaService.computeSlaStatus(complaint);
        ComplaintResponseDTO.CustomerInfo customerInfo = ComplaintResponseDTO.CustomerInfo.builder().name(complaint.getCustomer().getName()).email(maskEmail(complaint.getCustomer().getEmail())).build();
        ComplaintResponseDTO.AssignmentInfo assignmentInfo = null;
        if (assignment != null) assignmentInfo = ComplaintResponseDTO.AssignmentInfo.builder().departmentName(assignment.getDepartment().getName()).assignedAt(assignment.getAssignedAt()).status(assignment.getStatus().name()).build();
        return ComplaintResponseDTO.builder().ticketId(complaint.getTicketId()).title(complaint.getTitle()).status(complaint.getStatus()).channel(complaint.getChannel()).customer(customerInfo).assignment(assignmentInfo).slaDeadline(complaint.getSlaDeadline()).slaStatus(slaStatus).createdAt(complaint.getCreatedAt()).build();
    }

    private void logActivity(Long complaintId, String action, String performedBy, String notes) {
        activityRepository.save(ComplaintActivity.builder().complaintId(complaintId).action(action).performedBy(performedBy != null ? performedBy : "SYSTEM").notes(notes).performedAt(LocalDateTime.now()).build());
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@"); String local = parts[0];
        return (local.length() > 2 ? local.substring(0,2) + "***" : "***") + "@" + parts[1];
    }

}
