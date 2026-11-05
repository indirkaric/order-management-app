package com.sqa.oms.identity.service.permission.impl;

import com.sqa.oms.identity.dto.PermissionDTO;
import com.sqa.oms.identity.exception.EntityNotFoundException;
import com.sqa.oms.identity.mapper.IPermissionMapper;
import com.sqa.oms.identity.modules.permission.model.PermissionListRequest;
import com.sqa.oms.identity.repository.IPermissionRepository;
import com.sqa.oms.identity.repository.projection.PermissionProjection;
import com.sqa.oms.identity.service.model.PagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    private static final int PAGE_SIZE = 10;

    @Mock
    private IPermissionRepository permissionRepository;

    @Mock
    private IPermissionMapper permissionMapper;

    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionService(permissionRepository, permissionMapper);
    }

    @Test
    void getByEntityIdReturnsTheMatchingPermission() {
        PermissionProjection projection = new PermissionProjection("ORDER_READ", "Order Read", null, (short) 1);
        PermissionDTO dto = new PermissionDTO();
        when(permissionRepository.findProjectionByEntityId("ORDER_READ")).thenReturn(Optional.of(projection));
        when(permissionMapper.toDto(projection)).thenReturn(dto);

        assertThat(permissionService.getByEntityId("ORDER_READ")).isEqualTo(dto);
    }

    @Test
    void getByEntityIdThrowsWhenNotFound() {
        when(permissionRepository.findProjectionByEntityId("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.getByEntityId("UNKNOWN"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getPagedListDelegatesToTheRepository() {
        PermissionListRequest request = new PermissionListRequest(null, null, null);
        PermissionProjection projection = new PermissionProjection("ORDER_READ", "Order Read", null, (short) 1);
        PermissionDTO dto = new PermissionDTO();
        Page<PermissionProjection> page = new PageImpl<>(List.of(projection));
        when(permissionRepository.findPagedList(request, PageRequest.of(0, PAGE_SIZE))).thenReturn(page);
        when(permissionMapper.toDto(projection)).thenReturn(dto);

        PagedList<PermissionDTO> result = permissionService.getPagedList(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getItems()).containsExactly(dto);
        assertThat(result.getCount()).isEqualTo(1);
    }
}
