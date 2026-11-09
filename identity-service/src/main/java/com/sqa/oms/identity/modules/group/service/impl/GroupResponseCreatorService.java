package com.sqa.oms.identity.modules.group.service.impl;

import com.sqa.oms.identity.dto.GroupDTO;
import com.sqa.oms.identity.enums.ExpandOption;
import com.sqa.oms.identity.mapper.IGroupMapper;
import com.sqa.oms.identity.modules.group.model.GroupResponse;
import com.sqa.oms.identity.modules.group.service.IGroupResponseCreatorService;
import com.sqa.oms.identity.repository.IPermissionRepository;
import com.sqa.oms.identity.service.group.IGroupService;
import com.sqa.oms.identity.util.Check;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the single group response builder.
 */
@Service
public final class GroupResponseCreatorService implements IGroupResponseCreatorService {

    private final IGroupService groupService;

    private final IGroupMapper groupMapper;

    private final IPermissionRepository permissionRepository;

    /**
     * Constructor with dependency injection.
     *
     * @param groupService          core group read access
     * @param groupMapper           group dto mapper
     * @param permissionRepository  access to the permission table, used to expand permissions
     */
    public GroupResponseCreatorService(final IGroupService groupService, final IGroupMapper groupMapper,
                                       final IPermissionRepository permissionRepository) {
        Check.isNotNull(groupService, "groupService");
        Check.isNotNull(groupMapper, "groupMapper");
        Check.isNotNull(permissionRepository, "permissionRepository");

        this.groupService = groupService;
        this.groupMapper = groupMapper;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public GroupResponse create(final String entityId, final ExpandOption expand) {
        Check.isNotEmpty(entityId, "entityId");

        return buildResponse(groupService.getByEntityId(entityId), expand);
    }

    private GroupResponse buildResponse(final GroupDTO group, final ExpandOption expand) {
        GroupResponse response = groupMapper.toResponse(group);
        if (ExpandOption.PERMISSIONS == expand) {
            response.setPermissions(permissionRepository.findNamesByGroupEntityId(group.getEntityId()));
        }

        return response;
    }
}
