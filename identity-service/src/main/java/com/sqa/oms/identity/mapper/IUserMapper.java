package com.sqa.oms.identity.mapper;

import com.sqa.oms.identity.dto.UserDTO;
import com.sqa.oms.identity.entity.User;
import com.sqa.oms.identity.modules.user.model.UserResponse;
import com.sqa.oms.identity.repository.projection.UserProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Maps the user entity, projection, DTO and response models onto each other.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface IUserMapper {

    /**
     * Maps a single user, used only to build the domain event payload off the just
     * persisted entity.
     *
     * @param user entity to map
     * @return UserResponse
     */
    @Mapping(target = "permissionGroups", ignore = true)
    UserResponse toResponse(User user);

    /**
     * Maps a user projection to its DTO.
     *
     * @param projection projection to map
     * @return UserDTO
     */
    UserDTO toDto(UserProjection projection);

    /**
     * Maps a single user.
     *
     * @param dto source user dto
     * @return UserResponse
     */
    @Mapping(target = "permissionGroups", ignore = true)
    UserResponse toResponse(UserDTO dto);
}
