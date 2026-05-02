package com.complaintiq.common;
import org.slf4j.MDC;
public class MdcUtil {
    public static final String TICKET_ID_KEY = "ticketId";
    private MdcUtil() {}
    public static void setTicketId(String ticketId) { MDC.put(TICKET_ID_KEY, ticketId); }
    public static void clear() { MDC.remove(TICKET_ID_KEY); }
    public static void clearAll() { MDC.clear(); }
}
