package com.complaintiq.sla;
import com.complaintiq.agent.Agent;
import com.complaintiq.agent.AgentRepository;
import com.complaintiq.agent.enums.AgentRole;
import com.complaintiq.assignment.*;
import com.complaintiq.complaint.*;
import com.complaintiq.complaint.enums.ComplaintStatus;
import com.complaintiq.common.MdcUtil;
import com.complaintiq.escalation.*;
import com.complaintiq.escalation.enums.EscalationReason;
import com.complaintiq.notification.NotificationService;
import com.complaintiq.websocket.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
@Slf4j @Component @RequiredArgsConstructor
public class SLAScheduler {
    private final ComplaintRepository complaintRepository;
    private final AssignmentRepository assignmentRepository;
    private final AgentRepository agentRepository;
    private final EscalationRepository escalationRepository;
    private final SLAConfigRepository slaConfigRepository;
    private final ComplaintActivityRepository activityRepository;
    private final NotificationService notificationService;
    private final WebSocketController webSocketController;
    @Scheduled(fixedRateString="${app.sla.scheduler.fixed-rate-ms:300000}")
    @Transactional
    public void runSLACheck() {
        log.info("SLA check started at: {}", LocalDateTime.now());
        List<Complaint> activeComplaints = complaintRepository.findAllActiveForSlaCheck();
        if (activeComplaints.isEmpty()) { log.debug("No active complaints for SLA check."); return; }
        int warningCount=0, breachCount=0, skippedCount=0;
        for (Complaint complaint : activeComplaints) {
            try {
                MdcUtil.setTicketId(complaint.getTicketId());
                ProcessResult result = processComplaint(complaint);
                switch (result) { case WARNING -> warningCount++; case BREACHED -> breachCount++; case SKIPPED -> skippedCount++; default -> {} }
            } catch (Exception ex) { log.error("SLA check failed for ticketId={}: {}", complaint.getTicketId(), ex.getMessage(), ex); }
            finally { MdcUtil.clear(); }
        }
        log.info("SLA check complete — total={} warnings={} breaches={} skipped={}", activeComplaints.size(), warningCount, breachCount, skippedCount);
    }
    private ProcessResult processComplaint(Complaint complaint) {
        if (complaint.getSlaDeadline() == null || complaint.getUrgency() == null) return ProcessResult.SKIPPED;
        SLAConfig config = slaConfigRepository.findByUrgencyLevel(complaint.getUrgency()).orElse(null);
        if (config == null) return ProcessResult.SKIPPED;
        LocalDateTime now = LocalDateTime.now(); LocalDateTime createdAt = complaint.getCreatedAt();
        if (createdAt == null) return ProcessResult.SKIPPED;
        double hoursElapsed = ChronoUnit.MINUTES.between(createdAt, now) / 60.0;
        double slaHours = config.getResolutionHours();
        double warningAt = slaHours * (config.getWarningThresholdPercent() / 100.0);
        if (now.isAfter(complaint.getSlaDeadline())) { handleSLABreach(complaint, hoursElapsed, config); return ProcessResult.BREACHED; }
        if (hoursElapsed >= warningAt) { handleSLAWarning(complaint, hoursElapsed, slaHours); return ProcessResult.WARNING; }
        return ProcessResult.OK;
    }
    private void handleSLAWarning(Complaint complaint, double hoursElapsed, double slaHours) {
        log.warn("SLA WARNING: ticketId={} urgency={} hoursElapsed={}", complaint.getTicketId(), complaint.getUrgency(), String.format("%.1f",hoursElapsed));
        assignmentRepository.findByComplaintId(complaint.getId()).ifPresent(assignment -> notificationService.sendSlaWarningToAgent(assignment.getAgent(), complaint, hoursElapsed, slaHours));
        webSocketController.broadcastStatusUpdate(ComplaintStatusUpdate.builder().ticketId(complaint.getTicketId()).newStatus(complaint.getStatus().name()).message("SLA warning: approaching deadline").timestamp(LocalDateTime.now()).slaStatus("WARNING").build());
    }
    @Transactional
    private void handleSLABreach(Complaint complaint, double hoursElapsed, SLAConfig config) {
        String ticketId = complaint.getTicketId();
        log.error("SLA BREACHED: ticketId={} urgency={} hoursElapsed={}", ticketId, complaint.getUrgency(), String.format("%.1f",hoursElapsed));
        if (escalationRepository.existsByComplaintIdAndReason(complaint.getId(), EscalationReason.SLA_BREACH)) { log.debug("SLA breach already escalated: ticketId={}", ticketId); return; }
        Optional<Assignment> assignmentOpt = assignmentRepository.findByComplaintId(complaint.getId());
        Agent currentAgent = assignmentOpt.map(Assignment::getAgent).orElse(null);
        Agent escalationTarget = findEscalationTarget(complaint, currentAgent, config);
        if (escalationTarget == null) { log.error("No escalation target found: ticketId={}", ticketId); return; }
        complaint.setStatus(ComplaintStatus.ESCALATED); complaintRepository.save(complaint);
        Escalation escalation = Escalation.builder().complaint(complaint).escalatedFrom(currentAgent).escalatedTo(escalationTarget).reason(EscalationReason.SLA_BREACH).escalatedAt(LocalDateTime.now()).notes(String.format("Auto-escalated: SLA breached after %.1f hours. Urgency: %s", hoursElapsed, complaint.getUrgency())).build();
        escalationRepository.save(escalation);
        assignmentOpt.ifPresent(assignment -> {
            agentRepository.decrementLoadAndIncrementResolved(assignment.getAgent().getId());
            assignment.setAgent(escalationTarget); assignment.setReassignedAt(LocalDateTime.now()); assignment.setReassignReason("Auto-escalated due to SLA breach");
            assignmentRepository.save(assignment); agentRepository.incrementLoad(escalationTarget.getId());
        });
        activityRepository.save(ComplaintActivity.builder().complaintId(complaint.getId()).action("Auto-escalated due to SLA breach").performedBy("SYSTEM").notes(String.format("Escalated to %s after %.1f hours", escalationTarget.getName(), hoursElapsed)).performedAt(LocalDateTime.now()).build());
        log.info("Escalation created: ticketId={} escalatedTo={}", ticketId, escalationTarget.getName());
        notificationService.notifyAgentOfEscalation(escalationTarget, complaint, hoursElapsed);
        notificationService.notifyCustomerOfEscalation(complaint.getCustomer(), complaint);
        webSocketController.broadcastStatusUpdate(ComplaintStatusUpdate.builder().ticketId(ticketId).newStatus(ComplaintStatus.ESCALATED.name()).message("Your complaint has been escalated for priority resolution.").timestamp(LocalDateTime.now()).slaStatus("BREACHED").build());
    }
    private Agent findEscalationTarget(Complaint complaint, Agent currentAgent, SLAConfig config) {
        String chainStr = config.getEscalationChain();
        if (chainStr == null || chainStr.isBlank()) chainStr = "JUNIOR,SENIOR,TEAM_LEAD,MANAGER";
        String[] chain = chainStr.split(",");
        int currentIndex = -1;
        if (currentAgent != null) { for (int i = 0; i < chain.length; i++) { if (chain[i].trim().equalsIgnoreCase(currentAgent.getRole().name())) { currentIndex = i; break; } } }
        for (int i = currentIndex + 1; i < chain.length; i++) {
            try {
                AgentRole targetRole = AgentRole.valueOf(chain[i].trim().toUpperCase());
                if (currentAgent != null && currentAgent.getDepartment() != null) {
                    List<Agent> candidates = agentRepository.findByDepartmentAndRole(currentAgent.getDepartment(), targetRole);
                    if (!candidates.isEmpty()) return candidates.get(0);
                }
                List<Agent> anyCandidates = agentRepository.findAll().stream().filter(a -> a.getRole() == targetRole).filter(a -> Boolean.TRUE.equals(a.getIsAvailable())).toList();
                if (!anyCandidates.isEmpty()) return anyCandidates.get(0);
            } catch (IllegalArgumentException ex) { log.warn("Invalid role in escalation chain: {}", chain[i]); }
        }
        return agentRepository.findAll().stream().filter(a -> a.getRole() == AgentRole.MANAGER).findFirst().orElse(null);
    }
    private enum ProcessResult { OK, WARNING, BREACHED, SKIPPED }
}
