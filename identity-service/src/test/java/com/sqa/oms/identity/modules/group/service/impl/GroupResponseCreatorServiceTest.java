package com.sqa.oms.identity.modules.group.service.impl;

import com.sqa.oms.identity.dto.GroupDTO;
import com.sqa.oms.identity.enums.ExpandOption;
import com.sqa.oms.identity.mapper.IGroupMapper;
import com.sqa.oms.identity.modules.group.model.GroupResponse;
import com.sqa.oms.identity.repository.IPermissionRepository;
import com.sqa.oms.identity.service.group.IGroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupResponseCreatorServiceTest {

    @Mock
    private IGroupService groupService;

    @Mock
    private IGroupMapper groupMapper;

    @Mock
    private IPermissionRepository permissionRepository;

    private GroupResponseCreatorService responseCreatorService;

    @BeforeEach
    void setUp() {
        responseCreatorService = new GroupResponseCreatorService(groupService, groupMapper, permissionRepository);
    }

    @Test
    void createDoesNotExpandByDefault() {
        GroupDTO group = new GroupDTO();
        group.setEntityId("GROUP0001");
        GroupResponse response = new GroupResponse();

        when(groupService.getByEntityId("GROUP0001")).thenReturn(group);
        when(groupMapper.toResponse(group)).thenReturn(response);

        GroupResponse result = responseCreatorService.create("GROUP0001", null);

        assertThat(result.getPermissions()).isNull();
    }

    @Test
    void createExpandsPermissionsWhenRequested() {
        GroupDTO group = new GroupDTO();
        group.setEntityId("GROUP0001");
        GroupResponse response = new GroupResponse();

        when(groupService.getByEntityId("GROUP0001")).thenReturn(group);
        when(groupMapper.toResponse(group)).thenReturn(response);
        when(permissionRepository.findNamesByGroupEntityId("GROUP0001")).thenReturn(List.of("ORDER_READ"));

        GroupResponse result = responseCreatorService.create("GROUP0001", ExpandOption.PERMISSIONS);

        assertThat(result.getPermissions()).containsExactly("ORDER_READ");
    }
}
