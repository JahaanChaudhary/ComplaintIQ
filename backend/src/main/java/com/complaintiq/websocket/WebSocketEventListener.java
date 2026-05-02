package com.complaintiq.websocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;
@Slf4j @Component
public class WebSocketEventListener {
    @EventListener public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("WebSocket client connected: sessionId={}", StompHeaderAccessor.wrap(event.getMessage()).getSessionId());
    }
    @EventListener public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        log.info("WebSocket client disconnected: sessionId={}", StompHeaderAccessor.wrap(event.getMessage()).getSessionId());
    }
}
