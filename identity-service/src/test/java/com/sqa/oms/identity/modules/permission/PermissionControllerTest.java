package com.sqa.oms.identity.modules.permission;

import com.sqa.oms.identity.modules.permission.model.PermissionListRequest;
import com.sqa.oms.identity.modules.permission.model.PermissionListResponse;
import com.sqa.oms.identity.modules.permission.model.PermissionResponse;
import com.sqa.oms.identity.modules.permission.service.IPermissionListResponseCreatorService;
import com.sqa.oms.identity.modules.permission.service.IPermissionResponseCreatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionControllerTest {

    private static final int PAGE_SIZE = 50;

    @Mock
    private IPermissionResponseCreatorService permissionResponseCreatorService;

    @Mock
    private IPermissionListResponseCreatorService permissionListResponseCreatorService;

    private PermissionController permissionController;

    @BeforeEach
    void setUp() {
        permissionController = new PermissionController(permissionResponseCreatorService,
                permissionListResponseCreatorService);
    }

    @Test
    void getPermissionsDelegatesToTheResponseCreatorService() {
        PermissionListRequest request = new PermissionListRequest(null, null, null);
        PermissionListResponse response = new PermissionListResponse(List.of(new PermissionResponse()), 1);
        when(permissionListResponseCreatorService.create(request, PageRequest.of(0, PAGE_SIZE)))
                .thenReturn(response);

        ResponseEntity<PermissionListResponse> result =
                permissionController.getPermissions(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturnNoContentWhenNoPermissionsAreFound() {
        PermissionListRequest request = new PermissionListRequest(null, null, null);
        PermissionListResponse response = new PermissionListResponse(List.of(), 0);
        when(permissionListResponseCreatorService.create(request, PageRequest.of(0, PAGE_SIZE)))
                .thenReturn(response);

        ResponseEntity<PermissionListResponse> result =
                permissionController.getPermissions(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void getPermissionDelegatesToTheResponseCreatorService() {
        PermissionResponse response = new PermissionResponse();
        when(permissionResponseCreatorService.create("ORDER_READ")).thenReturn(response);

        ResponseEntity<PermissionResponse> result = permissionController.getPermission("order_read");

        assertThat(result.getBody()).isEqualTo(response);
    }
}
