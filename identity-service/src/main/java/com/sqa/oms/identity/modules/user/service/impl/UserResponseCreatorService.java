package com.sqa.oms.identity.modules.user.service.impl;

import com.sqa.oms.identity.dto.UserDTO;
import com.sqa.oms.identity.enums.ExpandOption;
import com.sqa.oms.identity.mapper.IGroupMapper;
import com.sqa.oms.identity.mapper.IUserMapper;
import com.sqa.oms.identity.modules.user.model.UserResponse;
import com.sqa.oms.identity.modules.user.service.IUserResponseCreatorService;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.service.user.IUserService;
import com.sqa.oms.identity.util.Check;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the single user response builder.
 */
@Service
public class UserResponseCreatorService implements IUserResponseCreatorService {

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
    public UserResponseCreatorService(final IUserService userService,
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
    public UserResponse create(final String entityId, final ExpandOption expand) {
        Check.isNotEmpty(entityId, "entityId");

        return buildResponse(userService.getByEntityId(entityId), expand);
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
