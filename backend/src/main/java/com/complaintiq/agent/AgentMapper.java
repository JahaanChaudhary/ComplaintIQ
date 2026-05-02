package com.complaintiq.agent;
import com.complaintiq.agent.dto.AgentResponseDTO;
import org.mapstruct.*;
@Mapper(componentModel="spring")
public interface AgentMapper {
    @Mapping(target="departmentName", source="department.name")
    @Mapping(target="departmentId", source="department.id")
    AgentResponseDTO toResponseDTO(Agent agent);
    @BeanMapping(nullValuePropertyMappingStrategy=NullValuePropertyMappingStrategy.IGNORE)
    void updateAgentFromDTO(AgentResponseDTO dto, @MappingTarget Agent agent);
}
