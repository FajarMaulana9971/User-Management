package com.unictive.usermanagement.mappers;

import com.unictive.usermanagement.dto.requests.RegisterRequest;
import com.unictive.usermanagement.dto.responses.UserResponse;
import com.unictive.usermanagement.entities.User;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = {HobbyMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "id",             ignore = true)
    @Mapping(target = "password",       ignore = true)
    @Mapping(target = "role",           ignore = true)
    @Mapping(target = "hobbies",        ignore = true)
    @Mapping(target = "profilePicture", ignore = true)
    @Mapping(target = "active",         ignore = true)
    @Mapping(target = "createdAt",      ignore = true)
    @Mapping(target = "updatedAt",      ignore = true)
    @Mapping(target = "createdBy",      ignore = true)
    @Mapping(target = "updatedBy",      ignore = true)
    User toEntity(RegisterRequest request);

    @Mapping(target = "role",              source = "role.name")
    @Mapping(target = "profilePictureUrl", source = "profilePicture")
    @Mapping(target = "isActive",          source = "active")
    UserResponse toResponse(User user);
}
