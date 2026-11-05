package com.sqa.oms.identity.service.group.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.entity.Permission;
import com.sqa.oms.identity.event.DomainChangeEvent;
import com.sqa.oms.identity.mapper.IGroupMapper;
import com.sqa.oms.identity.modules.group.model.CreateGroupRequest;
import com.sqa.oms.identity.modules.group.model.GroupResponse;
import com.sqa.oms.identity.repository.IGroupPermissionRepository;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.IPermissionRepository;
import com.sqa.oms.identity.security.ICurrentCallerProvider;
import com.sqa.oms.identity.service.validator.ICreateGroupServiceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateGroupServiceTest {

    @Mock
    private IGroupRepository groupRepository;

    @Mock
    private IPermissionRepository permissionRepository;

    @Mock
    private IGroupPermissionRepository groupPermissionRepository;

    @Mock
    private IGroupMapper groupMapper;

    @Mock
    private ICreateGroupServiceValidator validator;

    @Mock
    private ICurrentCallerProvider currentCallerProvider;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private CreateGroupService createGroupService;

    @BeforeEach
    void setUp() {
        createGroupService = new CreateGroupService(groupRepository, permissionRepository,
                groupPermissionRepository, groupMapper, validator, currentCallerProvider,
                applicationEventPublisher, new ObjectMapper());
    }

    @Test
    void saveValidatesAndAssignsEveryRequestedPermission() {
        CreateGroupRequest request = new CreateGroupRequest("OMS_MANAGER", "desc", List.of("ORDER_READ"));
        Permission permission = new Permission();
        permission.setEntityId("ORDER_READ");

        when(groupRepository.existsByEntityId(anyString())).thenReturn(false);
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(permissionRepository.findAllByEntityIdIn(List.of("ORDER_READ"))).thenReturn(List.of(permission));
        when(groupMapper.toResponse(any(Group.class))).thenReturn(new GroupResponse());

        String entityId = createGroupService.save(request);

        assertThat(entityId).isNotBlank();
        verify(validator).validate(request);
        verify(groupPermissionRepository).saveAll(any());
        verify(applicationEventPublisher).publishEvent(any(DomainChangeEvent.class));
    }
}
