package com.sqa.oms.identity.modules.user.service.impl;

import com.sqa.oms.identity.dto.UserDTO;
import com.sqa.oms.identity.enums.ExpandOption;
import com.sqa.oms.identity.mapper.IGroupMapper;
import com.sqa.oms.identity.mapper.IUserMapper;
import com.sqa.oms.identity.modules.user.model.UserListRequest;
import com.sqa.oms.identity.modules.user.model.UserListResponse;
import com.sqa.oms.identity.modules.user.model.UserResponse;
import com.sqa.oms.identity.modules.user.service.IUserListResponseCreatorService;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.service.model.PagedList;
import com.sqa.oms.identity.service.user.IUserService;
import com.sqa.oms.identity.util.Check;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of the user list response builder.
 */
@Service
public final class UserListResponseCreatorService implements IUserListResponseCreatorService {

    private final IUserService userService;

    private final IUserMapper userMapper;

    private final IGroupMapper groupMapper;

    private final IGroupRepository groupRepository;

    /**
     * Constructor with dependency injection.
     *
     * @param userService     core user read access
     * @param userMapper      user dto mapper
     * @param groupMapper     group projection mapper
     * @param groupRepository access to the group table, used to expand permission groups
     */
    public UserListResponseCreatorService(final IUserService userService,
                                          final IUserMapper userMapper,
                                          final IGroupMapper groupMapper,
                                          final IGroupRepository groupRepository) {
        Check.isNotNull(userService, "userService");
        Check.isNotNull(userMapper, "userMapper");
        Check.isNotNull(groupMapper, "groupMapper");
        Check.isNotNull(groupRepository, "groupRepository");

        this.userService = userService;
        this.userMapper = userMapper;
        this.groupMapper = groupMapper;
        this.groupRepository = groupRepository;
    }

    @Override
    public UserListResponse create(final UserListRequest request, final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        PagedList<UserDTO> pagedList = userService.getPagedList(request, pageable);
        List<UserResponse> data = pagedList.getItems().stream()
                .map(user -> buildResponse(user, request.getExpand()))
                .toList();

        return new UserListResponse(data, pagedList.getCount());
    }

    private UserResponse buildResponse(final UserDTO user, final ExpandOption expand) {
        UserResponse response = userMapper.toResponse(user);
        if (ExpandOption.PERMISSIONS_GROUPS == expand) {
            response.setPermissionGroups(groupRepository.findSummaryProjectionsByUserEntityId(user.getEntityId())
                    .stream()
                    .map(groupMapper::toSummaryResponse)
                    .toList());
        }

        return response;
    }
}
