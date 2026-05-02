package com.complaintiq.escalation;
import com.complaintiq.escalation.dto.EscalationResponseDTO;
import org.mapstruct.*;
@Mapper(componentModel="spring")
public interface EscalationMapper {
    @Mapping(target="ticketId", source="complaint.ticketId")
    @Mapping(target="escalatedFromName", source="escalatedFrom.name")
    @Mapping(target="escalatedToName", source="escalatedTo.name")
    EscalationResponseDTO toResponseDTO(Escalation escalation);
}
