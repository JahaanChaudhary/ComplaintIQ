package com.complaintiq.common;
import org.springframework.stereotype.Component;
import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;
@Component
public class TicketIdGenerator {
    private final AtomicLong sequence = new AtomicLong(0);
    public String generate() {
        long next = sequence.incrementAndGet();
        int year = Year.now().getValue();
        return String.format("CIQ-%d-%05d", year, next);
    }
    public void initSequence(long currentMax) { sequence.set(currentMax); }
}
