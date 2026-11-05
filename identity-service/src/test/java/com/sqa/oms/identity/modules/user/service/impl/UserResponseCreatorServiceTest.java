package com.sqa.oms.identity.modules.user.service.impl;

import com.sqa.oms.identity.dto.UserDTO;
import com.sqa.oms.identity.enums.ExpandOption;
import com.sqa.oms.identity.mapper.IGroupMapper;
import com.sqa.oms.identity.mapper.IUserMapper;
import com.sqa.oms.identity.modules.group.model.GroupSummaryResponse;
import com.sqa.oms.identity.modules.user.model.UserResponse;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.projection.GroupSummaryProjection;
import com.sqa.oms.identity.service.user.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserResponseCreatorServiceTest {

    @Mock
    private IUserService userService;

    @Mock
    private IUserMapper userMapper;

    @Mock
    private IGroupMapper groupMapper;

    @Mock
    private IGroupRepository groupRepository;

    private UserResponseCreatorService responseCreatorService;

    @BeforeEach
    void setUp() {
        responseCreatorService =
                new UserResponseCreatorService(userService, userMapper, groupMapper, groupRepository);
    }

    @Test
    void createDoesNotExpandByDefault() {
        UserDTO user = new UserDTO();
        user.setEntityId("USR00001");
        UserResponse response = new UserResponse();

        when(userService.getByEntityId("USR00001")).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = responseCreatorService.create("USR00001", null);

        assertThat(result.getPermissionGroups()).isNull();
    }

    @Test
    void createExpandsPermissionGroupsWhenRequested() {
        UserDTO user = new UserDTO();
        user.setEntityId("USR00001");
        GroupSummaryProjection projection = new GroupSummaryProjection("GROUP0001", "Group");

        UserResponse response = new UserResponse();
        GroupSummaryResponse summary = new GroupSummaryResponse();

        when(userService.getByEntityId("USR00001")).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);
        when(groupRepository.findSummaryProjectionsByUserEntityId("USR00001")).thenReturn(List.of(projection));
        when(groupMapper.toSummaryResponse(projection)).thenReturn(summary);

        UserResponse result = responseCreatorService.create("USR00001", ExpandOption.PERMISSIONS_GROUPS);

        assertThat(result.getPermissionGroups()).containsExactly(summary);
    }
}
