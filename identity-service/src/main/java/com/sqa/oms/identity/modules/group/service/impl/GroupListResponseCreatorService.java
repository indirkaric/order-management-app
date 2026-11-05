package com.sqa.oms.identity.modules.group.service.impl;

import com.sqa.oms.identity.dto.GroupDTO;
import com.sqa.oms.identity.enums.ExpandOption;
import com.sqa.oms.identity.mapper.IGroupMapper;
import com.sqa.oms.identity.modules.group.model.GroupListRequest;
import com.sqa.oms.identity.modules.group.model.GroupListResponse;
import com.sqa.oms.identity.modules.group.model.GroupResponse;
import com.sqa.oms.identity.modules.group.service.IGroupListResponseCreatorService;
import com.sqa.oms.identity.repository.IPermissionRepository;
import com.sqa.oms.identity.service.group.IGroupService;
import com.sqa.oms.identity.service.model.PagedList;
import com.sqa.oms.identity.util.Check;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of the group list response builder.
 */
@Service
public class GroupListResponseCreatorService implements IGroupListResponseCreatorService {

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
    public GroupListResponseCreatorService(final IGroupService groupService, final IGroupMapper groupMapper,
                                           final IPermissionRepository permissionRepository) {
        Check.isNotNull(groupService, "groupService");
        Check.isNotNull(groupMapper, "groupMapper");
        Check.isNotNull(permissionRepository, "permissionRepository");

        this.groupService = groupService;
        this.groupMapper = groupMapper;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public GroupListResponse create(final GroupListRequest request, final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        PagedList<GroupDTO> pagedList = groupService.getPagedList(request, pageable);
        List<GroupResponse> data = pagedList.getItems().stream()
                .map(group -> buildResponse(group, request.getExpand()))
                .toList();

        return new GroupListResponse(data, pagedList.getCount());
    }

    private GroupResponse buildResponse(final GroupDTO group, final ExpandOption expand) {
        GroupResponse response = groupMapper.toResponse(group);
        if (ExpandOption.PERMISSIONS == expand) {
            response.setPermissions(permissionRepository.findNamesByGroupEntityId(group.getEntityId()));
        }

        return response;
    }
}
