package com.unictive.usermanagement.mappers;

import com.unictive.usermanagement.dto.requests.HobbyRequest;
import com.unictive.usermanagement.dto.responses.HobbyResponse;
import com.unictive.usermanagement.entities.Hobby;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface HobbyMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "users",     ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Hobby toEntity(HobbyRequest request);

    HobbyResponse toResponse(Hobby hobby);
}
