package com.sqa.oms.ui.modules.identity;

import com.sqa.oms.ui.modules.identity.model.PermissionListRequest;
import com.sqa.oms.ui.modules.identity.model.PermissionListResponse;
import com.sqa.oms.ui.modules.identity.model.PermissionResponse;
import com.sqa.oms.ui.service.identity.IPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionControllerTest {

    private static final int PAGE_SIZE = 50;

    @Mock
    private IPermissionService permissionService;

    private PermissionController permissionController;

    @BeforeEach
    void setUp() {
        permissionController = new PermissionController(permissionService);
    }

    @Test
    void shouldDelegateToTheServiceWhenGettingAll() {
        PermissionListRequest request = new PermissionListRequest(null, null, null);
        PermissionListResponse response = new PermissionListResponse(List.of(new PermissionResponse()), 1);
        when(permissionService.getAll(request, PageRequest.of(0, PAGE_SIZE))).thenReturn(response);

        assertThat(permissionController.getAll(request, PageRequest.of(0, PAGE_SIZE)).getBody())
                .isEqualTo(response);
    }

    @Test
    void shouldReturnNoContentWhenNoPermissionsAreFound() {
        PermissionListRequest request = new PermissionListRequest(null, null, null);
        PermissionListResponse response = new PermissionListResponse(List.of(), 0);
        when(permissionService.getAll(request, PageRequest.of(0, PAGE_SIZE))).thenReturn(response);

        assertThat(permissionController.getAll(request, PageRequest.of(0, PAGE_SIZE)).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldDelegateToTheServiceWhenGettingById() {
        PermissionResponse response = new PermissionResponse();
        when(permissionService.getById("1")).thenReturn(response);

        assertThat(permissionController.getById("1").getBody()).isEqualTo(response);
    }
}
