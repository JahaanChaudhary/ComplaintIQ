package com.complaintiq.sla;
import com.complaintiq.complaint.Complaint;
import com.complaintiq.complaint.ComplaintRepository;
import com.complaintiq.complaint.enums.*;
import com.complaintiq.exception.SLAConfigNotFoundException;
import com.complaintiq.sla.dto.SLABreachDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j @Service @RequiredArgsConstructor
public class SLAService {
    private final SLAConfigRepository slaConfigRepository;
    private final ComplaintRepository complaintRepository;
    @Transactional
    public LocalDateTime calculateAndSetDeadline(Complaint complaint, UrgencyLevel urgency) {
        final UrgencyLevel effectiveUrgency = (urgency == null) ? UrgencyLevel.MEDIUM : urgency;
        SLAConfig config = slaConfigRepository.findByUrgencyLevel(effectiveUrgency)
                .orElseThrow(() -> new SLAConfigNotFoundException(effectiveUrgency));
        LocalDateTime deadline = complaint.getCreatedAt() != null ? complaint.getCreatedAt().plusHours(config.getResolutionHours()) : LocalDateTime.now().plusHours(config.getResolutionHours());
        complaint.setSlaDeadline(deadline);
        complaintRepository.save(complaint);
        log.info("SLA deadline set: ticketId={} urgency={} deadline={}", complaint.getTicketId(), urgency, deadline);
        return deadline;
    }
    public SlaStatus computeSlaStatus(Complaint complaint) {
        if (complaint.getSlaDeadline() == null) return SlaStatus.ON_TIME;
        if (complaint.getStatus() == ComplaintStatus.RESOLVED || complaint.getStatus() == ComplaintStatus.CLOSED) return SlaStatus.ON_TIME;
        LocalDateTime now = LocalDateTime.now(); LocalDateTime deadline = complaint.getSlaDeadline();
        if (now.isAfter(deadline)) return SlaStatus.BREACHED;
        UrgencyLevel urgency = complaint.getUrgency() != null ? complaint.getUrgency() : UrgencyLevel.MEDIUM;
        SLAConfig config = slaConfigRepository.findByUrgencyLevel(urgency).orElse(null);
        if (config == null) return SlaStatus.ON_TIME;
        long totalHours = config.getResolutionHours(); long warningPct = config.getWarningThresholdPercent();
        long warningHours = (totalHours * warningPct) / 100;
        LocalDateTime createdAt = complaint.getCreatedAt() != null ? complaint.getCreatedAt() : now.minusHours(1);
        long hoursElapsed = ChronoUnit.HOURS.between(createdAt, now);
        return hoursElapsed >= warningHours ? SlaStatus.WARNING : SlaStatus.ON_TIME;
    }
    public double computeHoursElapsed(Complaint complaint) {
        if (complaint.getCreatedAt() == null) return 0.0;
        return ChronoUnit.MINUTES.between(complaint.getCreatedAt(), LocalDateTime.now()) / 60.0;
    }
    public double computeHoursOverdue(Complaint complaint) {
        if (complaint.getSlaDeadline() == null) return 0.0;
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(complaint.getSlaDeadline())) return 0.0;
        return ChronoUnit.MINUTES.between(complaint.getSlaDeadline(), now) / 60.0;
    }
    public SLABreachDTO buildBreachDTO(Complaint complaint) {
        return SLABreachDTO.builder().ticketId(complaint.getTicketId()).urgency(complaint.getUrgency()).slaDeadline(complaint.getSlaDeadline()).breachedAt(LocalDateTime.now()).hoursElapsed(computeHoursElapsed(complaint)).hoursOverdue(computeHoursOverdue(complaint)).build();
    }
    @Transactional(readOnly=true) public SLAConfig getConfigForUrgency(UrgencyLevel urgency) {
        return slaConfigRepository.findByUrgencyLevel(urgency).orElseThrow(() -> new SLAConfigNotFoundException(urgency));
    }
    @Transactional(readOnly=true) public List<SLAConfig> getAllConfigs() { return slaConfigRepository.findAll(); }
    @Transactional(readOnly=true) public List<SLABreachDTO> getBreachedComplaints() {
        return complaintRepository.findSlaBreachedComplaints(LocalDateTime.now()).stream().map(this::buildBreachDTO).collect(Collectors.toList());
    }
    public List<String> parseEscalationChain(UrgencyLevel urgency) {
        return slaConfigRepository.findByUrgencyLevel(urgency).map(config -> List.of(config.getEscalationChain().split(","))).orElse(List.of("JUNIOR","SENIOR","TEAM_LEAD","MANAGER"));
    }
    public boolean configExists(UrgencyLevel urgency) { return slaConfigRepository.existsByUrgencyLevel(urgency); }
}
