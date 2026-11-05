package com.sqa.oms.identity.mapper;

import com.sqa.oms.identity.dto.GroupDTO;
import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.modules.group.model.GroupResponse;
import com.sqa.oms.identity.modules.group.model.GroupSummaryResponse;
import com.sqa.oms.identity.repository.projection.GroupProjection;
import com.sqa.oms.identity.repository.projection.GroupSummaryProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Maps the group entity, projection, DTO and response models onto each other.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface IGroupMapper {

    /**
     * Maps a single group, used only to build the domain event payload off the just
     * persisted entity.
     *
     * @param group entity to map
     * @return GroupResponse
     */
    @Mapping(target = "permissions", ignore = true)
    GroupResponse toResponse(Group group);

    /**
     * Maps a group projection to its DTO.
     *
     * @param projection projection to map
     * @return GroupDTO
     */
    GroupDTO toDto(GroupProjection projection);

    /**
     * Maps a single group.
     *
     * @param dto source group dto
     * @return GroupResponse
     */
    @Mapping(target = "permissions", ignore = true)
    GroupResponse toResponse(GroupDTO dto);

    /**
     * Maps a group summary projection to its short response form, embedded in the user response.
     *
     * @param projection projection to map
     * @return GroupSummaryResponse
     */
    GroupSummaryResponse toSummaryResponse(GroupSummaryProjection projection);
}
