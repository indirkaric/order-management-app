package com.sqa.oms.identity.mapper;

import com.sqa.oms.identity.dto.PermissionDTO;
import com.sqa.oms.identity.modules.permission.model.PermissionResponse;
import com.sqa.oms.identity.repository.projection.PermissionProjection;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Maps the permission projection, DTO and response models onto each other.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface IPermissionMapper {

    /**
     * Maps a permission projection to its DTO.
     *
     * @param projection projection to map
     * @return PermissionDTO
     */
    PermissionDTO toDto(PermissionProjection projection);

    /**
     * Maps a single permission.
     *
     * @param dto source permission dto
     * @return PermissionResponse
     */
    PermissionResponse toResponse(PermissionDTO dto);

    /**
     * Maps a list of permissions.
     *
     * @param dtos source permission dtos
     * @return List of PermissionResponse
     */
    List<PermissionResponse> toResponseList(List<PermissionDTO> dtos);
}
