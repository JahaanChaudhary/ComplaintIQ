package com.complaintiq.exception;
import com.complaintiq.complaint.enums.ComplaintStatus;
public class ComplaintStateException extends RuntimeException {
    public ComplaintStateException(String ticketId, ComplaintStatus current, ComplaintStatus attempted) {
        super(String.format("Cannot transition complaint [%s] from %s to %s", ticketId, current, attempted));
    }
    public ComplaintStateException(String message) { super(message); }
}
