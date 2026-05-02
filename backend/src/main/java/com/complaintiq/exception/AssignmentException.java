package com.complaintiq.exception;
public class AssignmentException extends RuntimeException {
    private final String ticketId;
    public AssignmentException(String ticketId, String message) {
        super(String.format("Assignment failed for ticket [%s]: %s", ticketId, message)); this.ticketId=ticketId;
    }
    public String getTicketId(){return ticketId;}
}
