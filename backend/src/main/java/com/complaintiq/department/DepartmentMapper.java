package com.complaintiq.department;
import com.complaintiq.department.dto.DepartmentDTO;
import org.mapstruct.*;
@Mapper(componentModel="spring")
public interface DepartmentMapper {
    @Mapping(target="agentCount", ignore=true)
    @Mapping(target="headAgentName", ignore=true)
    DepartmentDTO toDTO(Department department);
    @BeanMapping(nullValuePropertyMappingStrategy=NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDTO(DepartmentDTO dto, @MappingTarget Department department);
}
