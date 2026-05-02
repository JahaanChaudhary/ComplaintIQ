package com.complaintiq.auth;
import com.complaintiq.auth.dto.UserProfileDTO;
import org.mapstruct.*;
@Mapper(componentModel="spring")
public interface AuthMapper {
    @Mapping(target="role", expression="java(user.getRole().name())")
    UserProfileDTO toProfileDTO(AppUser user);
}
