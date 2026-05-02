package com.complaintiq.websocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.time.LocalDateTime;
@Slf4j @Controller @RequiredArgsConstructor
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    public void broadcastStatusUpdate(ComplaintStatusUpdate update) {
        if (update == null || update.getTicketId() == null) return;
        String destination = "/topic/complaint/" + update.getTicketId();
        messagingTemplate.convertAndSend(destination, update);
        log.debug("WebSocket broadcast: ticketId={} status={}", update.getTicketId(), update.getNewStatus());
    }
    public void broadcastSystemMessage(String message) {
        if (message == null || message.isBlank()) return;
        messagingTemplate.convertAndSend("/topic/system", ComplaintStatusUpdate.builder().ticketId("SYSTEM").newStatus("SYSTEM_MESSAGE").message(message).build());
    }
    @MessageMapping("/complaint/{ticketId}/subscribe")
    @SendTo("/topic/complaint/{ticketId}")
    public ComplaintStatusUpdate handleSubscription(@DestinationVariable String ticketId) {
        return ComplaintStatusUpdate.builder().ticketId(ticketId).newStatus("SUBSCRIBED").message("Connected. Listening for updates on " + ticketId).slaStatus("UNKNOWN").build();
    }
    public void notifyAgent(String agentEmail, String ticketId, String message) {
        if (agentEmail == null || agentEmail.isBlank()) return;
        messagingTemplate.convertAndSendToUser(agentEmail, "/queue/notifications", ComplaintStatusUpdate.builder().ticketId(ticketId).newStatus("AGENT_NOTIFICATION").message(message).build());
    }
}
