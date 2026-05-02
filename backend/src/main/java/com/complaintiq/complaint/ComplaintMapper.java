package com.complaintiq.complaint;
import com.complaintiq.complaint.dto.ComplaintSummaryDTO;
import org.mapstruct.*;
@Mapper(componentModel="spring")
public interface ComplaintMapper {
    @Mapping(target="customerName", source="customer.name")
    @Mapping(target="customerEmail", source="customer.email")
    @Mapping(target="customerTier", expression="java(complaint.getCustomer().getTier().name())")
    @Mapping(target="category", ignore=true)
    @Mapping(target="assignedAgentName", ignore=true)
    @Mapping(target="departmentName", ignore=true)
    @Mapping(target="slaStatus", ignore=true)
    ComplaintSummaryDTO toSummaryDTO(Complaint complaint);
}
