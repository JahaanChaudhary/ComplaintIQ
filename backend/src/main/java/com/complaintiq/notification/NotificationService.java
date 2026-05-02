package com.complaintiq.notification;
import com.complaintiq.agent.Agent;
import com.complaintiq.ai.AIAnalysis;
import com.complaintiq.assignment.Assignment;
import com.complaintiq.complaint.Complaint;
import com.complaintiq.customer.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
@Slf4j @Service @RequiredArgsConstructor
public class NotificationService {
    private final EmailService emailService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    @Async("taskExecutor") public void notifyCustomerOfComplaintReceived(Complaint complaint) {
        try {
            Customer customer = complaint.getCustomer();
            if (customer == null || customer.getEmail() == null) return;
            emailService.sendComplaintReceivedEmail(customer.getEmail(), customer.getName(), complaint.getTicketId(), complaint.getTitle());
            log.info("Customer notified of receipt: ticketId={}", complaint.getTicketId());
        } catch (Exception ex) { log.error("Failed to notify customer of receipt: ticketId={}", complaint.getTicketId()); }
    }
    @Async("taskExecutor") public void notifyAgentOfAssignment(Assignment assignment, Complaint complaint, AIAnalysis aiAnalysis) {
        try {
            Agent agent = assignment.getAgent();
            if (agent == null || agent.getEmail() == null) return;
            String slaDeadline = complaint.getSlaDeadline() != null ? complaint.getSlaDeadline().format(FORMATTER) : "Not set";
            emailService.sendAgentAssignmentEmail(agent.getEmail(), agent.getName(), complaint.getTicketId(), complaint.getCustomer().getName(), aiAnalysis.getUrgency().name(), aiAnalysis.getCategory().name(), aiAnalysis.getSentiment().name(), aiAnalysis.getSuggestedAction() != null ? aiAnalysis.getSuggestedAction() : "Review and resolve promptly.", slaDeadline);
            log.info("Agent notified of assignment: ticketId={} agent={}", complaint.getTicketId(), agent.getName());
        } catch (Exception ex) { log.error("Failed to notify agent of assignment: ticketId={}", complaint.getTicketId()); }
    }
    @Async("taskExecutor") public void notifyAgentOfReassignment(Assignment assignment, Complaint complaint) {
        try {
            Agent agent = assignment.getAgent(); if (agent == null || agent.getEmail() == null) return;
            emailService.sendReassignmentEmail(agent.getEmail(), agent.getName(), complaint.getTicketId(), assignment.getReassignReason() != null ? assignment.getReassignReason() : "Manual reassignment");
        } catch (Exception ex) { log.error("Failed to notify agent of reassignment: ticketId={}", complaint.getTicketId()); }
    }
    @Async("taskExecutor") public void sendSlaWarningToAgent(Agent agent, Complaint complaint, double hoursElapsed, double slaHours) {
        try {
            if (agent == null || agent.getEmail() == null) return;
            emailService.sendSlaWarningEmail(agent.getEmail(), agent.getName(), complaint.getTicketId(), complaint.getUrgency() != null ? complaint.getUrgency().name() : "MEDIUM", hoursElapsed, slaHours);
        } catch (Exception ex) { log.error("Failed to send SLA warning: ticketId={}", complaint.getTicketId()); }
    }
    @Async("taskExecutor") public void notifyAgentOfEscalation(Agent agent, Complaint complaint, double hoursElapsed) {
        try {
            if (agent == null || agent.getEmail() == null) return;
            emailService.sendEscalationToAgentEmail(agent.getEmail(), agent.getName(), complaint.getTicketId(), complaint.getUrgency() != null ? complaint.getUrgency().name() : "CRITICAL", hoursElapsed);
        } catch (Exception ex) { log.error("Failed to notify agent of escalation: ticketId={}", complaint.getTicketId()); }
    }
    @Async("taskExecutor") public void notifyCustomerOfEscalation(Customer customer, Complaint complaint) {
        try {
            if (customer == null || customer.getEmail() == null) return;
            emailService.sendCustomerEscalationEmail(customer.getEmail(), customer.getName(), complaint.getTicketId());
        } catch (Exception ex) { log.error("Failed to notify customer of escalation: ticketId={}", complaint.getTicketId()); }
    }
    @Async("taskExecutor") public void notifyCustomerOfResolution(Complaint complaint, String resolutionNote, String resolutionType) {
        try {
            Customer customer = complaint.getCustomer(); if (customer == null || customer.getEmail() == null) return;
            emailService.sendResolutionEmail(customer.getEmail(), customer.getName(), complaint.getTicketId(), resolutionNote, resolutionType);
        } catch (Exception ex) { log.error("Failed to notify customer of resolution: ticketId={}", complaint.getTicketId()); }
    }
    @Async("taskExecutor") public void sendFeedbackRequest(Complaint complaint) {
        try {
            Customer customer = complaint.getCustomer(); if (customer == null || customer.getEmail() == null) return;
            emailService.sendFeedbackRequestEmail(customer.getEmail(), customer.getName(), complaint.getTicketId());
        } catch (Exception ex) { log.error("Failed to send feedback request: ticketId={}", complaint.getTicketId()); }
    }
    @Async("taskExecutor") public void notifyAgentOfReopening(Agent agent, Complaint complaint, int satisfactionScore) {
        try {
            if (agent == null || agent.getEmail() == null) return;
            emailService.sendReopenedEmail(agent.getEmail(), agent.getName(), complaint.getTicketId(), satisfactionScore);
        } catch (Exception ex) { log.error("Failed to notify agent of reopening: ticketId={}", complaint.getTicketId()); }
    }
    @Async("taskExecutor") public void sendSmsNotification(String phone, String message) { log.info("SMS stub called: phone={}", phone); }
}
