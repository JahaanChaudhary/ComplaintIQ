package com.complaintiq.escalation;
import com.complaintiq.common.PagedResponse;
import com.complaintiq.escalation.dto.EscalationResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j @Service @RequiredArgsConstructor
public class EscalationService {
    private final EscalationRepository escalationRepository;
    @Transactional(readOnly=true) public List<EscalationResponseDTO> getByTicketId(String ticketId) { return escalationRepository.findByComplaintTicketId(ticketId).stream().map(this::mapToDTO).collect(Collectors.toList()); }
    @Transactional(readOnly=true) public PagedResponse<EscalationResponseDTO> getByAgentId(Long agentId, int page, int size) {
        Page<Escalation> escalations = escalationRepository.findByEscalatedToId(agentId, PageRequest.of(page, size, Sort.by("escalatedAt").descending()));
        return PagedResponse.from(escalations, escalations.getContent().stream().map(this::mapToDTO).collect(Collectors.toList()));
    }
    @Transactional(readOnly=true) public long countSlaBreaches() { return escalationRepository.countSlaBreaches(); }
    public EscalationResponseDTO mapToDTO(Escalation escalation) {
        return EscalationResponseDTO.builder().id(escalation.getId()).ticketId(escalation.getComplaint() != null ? escalation.getComplaint().getTicketId() : null).escalatedFromName(escalation.getEscalatedFrom() != null ? escalation.getEscalatedFrom().getName() : "SYSTEM").escalatedToName(escalation.getEscalatedTo() != null ? escalation.getEscalatedTo().getName() : null).reason(escalation.getReason()).notes(escalation.getNotes()).escalatedAt(escalation.getEscalatedAt()).build();
    }
}
