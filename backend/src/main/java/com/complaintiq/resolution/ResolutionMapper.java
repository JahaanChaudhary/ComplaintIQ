package com.complaintiq.resolution;
import com.complaintiq.resolution.dto.ResolutionResponseDTO;
import org.mapstruct.*;
@Mapper(componentModel="spring")
public interface ResolutionMapper {
    @Mapping(target="ticketId", source="complaint.ticketId")
    @Mapping(target="resolvedByName", source="resolvedBy.name")
    ResolutionResponseDTO toResponseDTO(Resolution resolution);
}
