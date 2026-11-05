package com.sqa.oms.identity.modules.permission.service.impl;

import com.sqa.oms.identity.dto.PermissionDTO;
import com.sqa.oms.identity.mapper.IPermissionMapper;
import com.sqa.oms.identity.modules.permission.model.PermissionResponse;
import com.sqa.oms.identity.service.permission.IPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionResponseCreatorServiceTest {

    @Mock
    private IPermissionService permissionService;

    @Mock
    private IPermissionMapper permissionMapper;

    private PermissionResponseCreatorService responseCreatorService;

    @BeforeEach
    void setUp() {
        responseCreatorService = new PermissionResponseCreatorService(permissionService, permissionMapper);
    }

    @Test
    void createReturnsTheMappedPermission() {
        PermissionDTO permission = new PermissionDTO();
        permission.setEntityId("ORDER_READ");
        PermissionResponse expected = new PermissionResponse();

        when(permissionService.getByEntityId("ORDER_READ")).thenReturn(permission);
        when(permissionMapper.toResponse(permission)).thenReturn(expected);

        assertThat(responseCreatorService.create("ORDER_READ")).isEqualTo(expected);
    }
}
