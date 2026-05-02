package com.complaintiq.customer;
import com.complaintiq.complaint.dto.ComplaintResponseDTO;
import org.mapstruct.*;
@Mapper(componentModel="spring")
public interface CustomerMapper {
    @Mapping(target="tier", expression="java(customer.getTier().name())")
    ComplaintResponseDTO.CustomerInfo toCustomerInfo(Customer customer);
}
