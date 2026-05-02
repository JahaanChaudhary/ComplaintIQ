package com.complaintiq.exception;
import com.complaintiq.complaint.enums.UrgencyLevel;
public class SLAConfigNotFoundException extends RuntimeException {
    public SLAConfigNotFoundException(UrgencyLevel urgencyLevel) {
        super(String.format("SLA configuration not found for urgency level: %s", urgencyLevel));
    }
}
