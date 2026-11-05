package com.sqa.oms.identity.modules.permission.service.impl;

import com.sqa.oms.identity.dto.PermissionDTO;
import com.sqa.oms.identity.mapper.IPermissionMapper;
import com.sqa.oms.identity.modules.permission.model.PermissionListRequest;
import com.sqa.oms.identity.modules.permission.model.PermissionListResponse;
import com.sqa.oms.identity.modules.permission.model.PermissionResponse;
import com.sqa.oms.identity.service.model.PagedList;
import com.sqa.oms.identity.service.permission.IPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionListResponseCreatorServiceTest {

    private static final int PAGE_SIZE = 10;

    @Mock
    private IPermissionService permissionService;

    @Mock
    private IPermissionMapper permissionMapper;

    private PermissionListResponseCreatorService responseCreatorService;

    @BeforeEach
    void setUp() {
        responseCreatorService = new PermissionListResponseCreatorService(permissionService, permissionMapper);
    }

    @Test
    void createMapsThePageToAListResponse() {
        PermissionDTO permission = new PermissionDTO();
        permission.setEntityId("ORDER_READ");
        PermissionListRequest request = new PermissionListRequest(null, null, null);

        when(permissionService.getPagedList(request, PageRequest.of(0, PAGE_SIZE)))
                .thenReturn(new PagedList<>(List.of(permission), 1));
        when(permissionMapper.toResponseList(List.of(permission)))
                .thenReturn(List.of(new PermissionResponse()));

        PermissionListResponse response = responseCreatorService.create(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(response.getData()).hasSize(1);
        assertThat(response.getCount()).isEqualTo(1);
    }
}
