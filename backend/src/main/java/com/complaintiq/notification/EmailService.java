package com.complaintiq.notification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
@Slf4j @Service @RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}") private String fromEmail;
    @Value("${spring.application.name:ComplaintIQ}") private String appName;
    @Async("taskExecutor")
    public void sendEmail(String to, String subject, String htmlBody) {
        if (to == null || to.isBlank()) return;
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, appName); helper.setTo(to); helper.setSubject(subject); helper.setText(htmlBody, true);
            mailSender.send(message); log.info("Email sent: to={} subject={}", to, subject);
        } catch (Exception ex) { log.error("Failed to send email: to={} error={}", to, ex.getMessage()); }
    }
    @Async("taskExecutor") public void sendComplaintReceivedEmail(String to, String name, String ticketId, String title) {
        sendEmail(to, "[" + appName + "] Complaint Received — " + ticketId, buildHtml("Complaint Received", name, "<p>Your complaint has been received.</p><p><strong>Ticket ID:</strong> " + ticketId + "</p><p><strong>Title:</strong> " + escapeHtml(title) + "</p>"));
    }
    @Async("taskExecutor") public void sendAgentAssignmentEmail(String to, String name, String ticketId, String customer, String urgency, String category, String sentiment, String action, String sla) {
        sendEmail(to, "[" + appName + "] New Complaint Assigned — " + ticketId + " [" + urgency + "]", buildHtml("New Complaint Assigned", name, "<p>A new complaint has been assigned to you.</p><p><strong>Ticket:</strong> " + ticketId + "</p><p><strong>Customer:</strong> " + escapeHtml(customer) + "</p><p><strong>Urgency:</strong> <span style='color:" + getUrgencyColor(urgency) + "'>" + urgency + "</span></p><p><strong>Category:</strong> " + category + "</p><p><strong>SLA Deadline:</strong> " + sla + "</p><p><strong>Suggested Action:</strong> " + escapeHtml(action) + "</p>"));
    }
    @Async("taskExecutor") public void sendSlaWarningEmail(String to, String name, String ticketId, String urgency, double hoursElapsed, double slaHours) {
        sendEmail(to, "[" + appName + "] SLA Warning — " + ticketId, buildHtml("SLA Warning", name, "<p>Complaint " + ticketId + " is approaching SLA deadline.</p><p><strong>Hours Elapsed:</strong> " + String.format("%.1f", hoursElapsed) + "</p><p><strong>SLA Hours:</strong> " + slaHours + "</p>"));
    }
    @Async("taskExecutor") public void sendEscalationToAgentEmail(String to, String name, String ticketId, String urgency, double hoursElapsed) {
        sendEmail(to, "[" + appName + "] Escalated Complaint — " + ticketId, buildHtml("Complaint Escalated To You", name, "<p>Complaint " + ticketId + " has been escalated to you due to SLA breach.</p><p><strong>Hours Elapsed:</strong> " + String.format("%.1f", hoursElapsed) + "</p>"));
    }
    @Async("taskExecutor") public void sendCustomerEscalationEmail(String to, String name, String ticketId) {
        sendEmail(to, "[" + appName + "] Update on Your Complaint — " + ticketId, buildHtml("Complaint Update", name, "<p>Your complaint " + ticketId + " has been escalated to our senior team for priority resolution.</p>"));
    }
    @Async("taskExecutor") public void sendResolutionEmail(String to, String name, String ticketId, String note, String type) {
        sendEmail(to, "[" + appName + "] Complaint Resolved — " + ticketId, buildHtml("Complaint Resolved", name, "<p>Your complaint " + ticketId + " has been resolved.</p><p><strong>Resolution:</strong> " + escapeHtml(note) + "</p><p><strong>Type:</strong> " + type + "</p>"));
    }
    @Async("taskExecutor") public void sendFeedbackRequestEmail(String to, String name, String ticketId) {
        sendEmail(to, "[" + appName + "] How did we do? — " + ticketId, buildHtml("Share Your Feedback", name, "<p>Your complaint " + ticketId + " has been resolved. Please rate your experience (1-5) via the portal.</p>"));
    }
    @Async("taskExecutor") public void sendReassignmentEmail(String to, String name, String ticketId, String reason) {
        sendEmail(to, "[" + appName + "] Complaint Reassigned — " + ticketId, buildHtml("Complaint Reassigned To You", name, "<p>Complaint " + ticketId + " has been reassigned to you.</p><p><strong>Reason:</strong> " + escapeHtml(reason) + "</p>"));
    }
    @Async("taskExecutor") public void sendReopenedEmail(String to, String name, String ticketId, int score) {
        sendEmail(to, "[" + appName + "] Complaint Reopened — " + ticketId, buildHtml("Complaint Reopened", name, "<p>Complaint " + ticketId + " has been reopened due to low satisfaction score: " + score + "/5.</p>"));
    }
    private String buildHtml(String heading, String name, String content) {
        return "<!DOCTYPE html><html><head><style>body{font-family:Arial,sans-serif;background:#f4f6f9}.container{max-width:600px;margin:30px auto;background:#fff;border-radius:8px;overflow:hidden}.header{background:linear-gradient(135deg,#2c3e50,#3498db);color:white;padding:24px 30px}.header h1{margin:0;font-size:22px}.body{padding:28px 30px;color:#333}.footer{background:#f4f6f9;padding:16px 30px;font-size:12px;color:#888;text-align:center}</style></head><body><div class=container><div class=header><h1>ComplaintIQ — " + heading + "</h1></div><div class=body><p>Dear <strong>" + name + "</strong>,</p>" + content + "</div><div class=footer>This is an automated message from ComplaintIQ.</div></div></body></html>";
    }
    private String getUrgencyColor(String urgency) {
        if (urgency == null) return "#333";
        return switch (urgency.toUpperCase()) { case "CRITICAL" -> "#e74c3c"; case "HIGH" -> "#e67e22"; case "MEDIUM" -> "#f39c12"; case "LOW" -> "#27ae60"; default -> "#333"; };
    }
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"", "&quot;");
    }
}
