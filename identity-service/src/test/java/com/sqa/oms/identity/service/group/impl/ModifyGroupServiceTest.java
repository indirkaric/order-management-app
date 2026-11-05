package com.sqa.oms.identity.service.group.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.entity.GroupPermission;
import com.sqa.oms.identity.entity.Permission;
import com.sqa.oms.identity.entity.id.GroupPermissionId;
import com.sqa.oms.identity.exception.EntityNotFoundException;
import com.sqa.oms.identity.mapper.IGroupMapper;
import com.sqa.oms.identity.modules.group.model.GroupResponse;
import com.sqa.oms.identity.modules.group.model.UpdateGroupRequest;
import com.sqa.oms.identity.repository.IGroupPermissionRepository;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.IPermissionRepository;
import com.sqa.oms.identity.security.ICurrentCallerProvider;
import com.sqa.oms.identity.service.validator.IModifyGroupServiceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModifyGroupServiceTest {

    @Mock
    private IGroupRepository groupRepository;

    @Mock
    private IPermissionRepository permissionRepository;

    @Mock
    private IGroupPermissionRepository groupPermissionRepository;

    @Mock
    private IGroupMapper groupMapper;

    @Mock
    private IModifyGroupServiceValidator validator;

    @Mock
    private ICurrentCallerProvider currentCallerProvider;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private ModifyGroupService modifyGroupService;

    @BeforeEach
    void setUp() {
        modifyGroupService = new ModifyGroupService(groupRepository, permissionRepository,
                groupPermissionRepository, groupMapper, validator, currentCallerProvider,
                applicationEventPublisher, new ObjectMapper());
    }

    @Test
    void updateThrowsWhenGroupDoesNotExist() {
        UpdateGroupRequest request = new UpdateGroupRequest("name", "desc", (short) 1, List.of("ORDER_READ"));
        when(groupRepository.findByEntityId("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> modifyGroupService.update("UNKNOWN", request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateReplacesTheWholePermissionSet() {
        Group group = new Group();
        group.setEntityId("GROUP0001");
        group.setName("OMS_MANAGER");

        Permission oldPermission = new Permission();
        oldPermission.setEntityId("ORDER_DELETE");
        GroupPermission oldAssignment = new GroupPermission();
        oldAssignment.setGroupPermissionId(new GroupPermissionId(group, oldPermission));

        Permission newPermission = new Permission();
        newPermission.setEntityId("ORDER_READ");

        UpdateGroupRequest request = new UpdateGroupRequest(
                "OMS_MANAGER", "desc", (short) 1, List.of("ORDER_READ"));

        when(groupRepository.findByEntityId("GROUP0001")).thenReturn(Optional.of(group));
        when(groupRepository.save(any(Group.class))).thenReturn(group);
        when(groupPermissionRepository.findByGroup(group)).thenReturn(List.of(oldAssignment));
        when(permissionRepository.findAllByEntityIdIn(List.of("ORDER_READ"))).thenReturn(List.of(newPermission));
        when(groupMapper.toResponse(any(Group.class))).thenReturn(new GroupResponse());

        modifyGroupService.update("GROUP0001", request);

        verify(validator).validate(group, request);

        ArgumentCaptor<List<GroupPermission>> deletedCaptor = ArgumentCaptor.forClass(List.class);
        verify(groupPermissionRepository).deleteAll(deletedCaptor.capture());
        assertThat(deletedCaptor.getValue()).containsExactly(oldAssignment);

        ArgumentCaptor<List<GroupPermission>> savedCaptor = ArgumentCaptor.forClass(List.class);
        verify(groupPermissionRepository).saveAll(savedCaptor.capture());
        assertThat(savedCaptor.getValue()).hasSize(1);
    }
}
