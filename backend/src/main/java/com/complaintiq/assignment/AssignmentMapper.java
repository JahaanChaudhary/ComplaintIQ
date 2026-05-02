package com.complaintiq.assignment;
import com.complaintiq.assignment.dto.AssignmentResponseDTO;
import com.complaintiq.complaint.dto.ComplaintResponseDTO;
import org.mapstruct.*;
@Mapper(componentModel="spring")
public interface AssignmentMapper {
    @Mapping(target="agentId", source="agent.id")
    @Mapping(target="agentName", source="agent.name")
    @Mapping(target="departmentId", source="department.id")
    @Mapping(target="departmentName", source="department.name")
    @Mapping(target="ticketId", source="complaint.ticketId")
    AssignmentResponseDTO toResponseDTO(Assignment assignment);
    @Mapping(target="agentId", source="agent.id")
    @Mapping(target="agentName", source="agent.name")
    @Mapping(target="departmentName", source="department.name")
    @Mapping(target="status", expression="java(assignment.getStatus().name())")
    ComplaintResponseDTO.AssignmentInfo toAssignmentInfo(Assignment assignment);
}
