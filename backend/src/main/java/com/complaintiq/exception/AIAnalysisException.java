package com.complaintiq.exception;
public class AIAnalysisException extends RuntimeException {
    private final String ticketId;
    public AIAnalysisException(String ticketId, String message) {
        super(String.format("AI analysis failed for ticket [%s]: %s", ticketId, message)); this.ticketId=ticketId;
    }
    public AIAnalysisException(String ticketId, String message, Throwable cause) {
        super(String.format("AI analysis failed for ticket [%s]: %s", ticketId, message), cause); this.ticketId=ticketId;
    }
    public String getTicketId(){return ticketId;}
}
