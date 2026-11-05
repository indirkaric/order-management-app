package com.sqa.oms.ui.service.identity.impl;

import com.sqa.oms.ui.client.identity.IdentityClient;
import com.sqa.oms.ui.client.response.ClientResponseWrapper;
import com.sqa.oms.ui.modules.identity.model.PermissionListRequest;
import com.sqa.oms.ui.modules.identity.model.PermissionListResponse;
import com.sqa.oms.ui.modules.identity.model.PermissionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    private static final int PAGE_SIZE = 50;

    @Mock
    private IdentityClient identityClient;

    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionService(identityClient);
    }

    @Test
    void shouldReturnTheMappedListWhenGettingAll() {
        PermissionListResponse response = new PermissionListResponse(List.of(), 0);
        PermissionListRequest request = new PermissionListRequest(null, null, null);

        when(identityClient.getPermissions(any(), any()))
                .thenReturn(new ClientResponseWrapper(HttpStatus.OK, response));

        assertThat(permissionService.getAll(request, PageRequest.of(0, PAGE_SIZE))).isEqualTo(response);
    }

    @Test
    void shouldReturnAnEmptyResponseWhenTheIdentityServiceReturnsNoContentOnGettingAll() {
        PermissionListRequest request = new PermissionListRequest(null, null, null);

        when(identityClient.getPermissions(any(), any()))
                .thenReturn(new ClientResponseWrapper(HttpStatus.NO_CONTENT, null));

        assertThat(permissionService.getAll(request, PageRequest.of(0, PAGE_SIZE)).getData()).isEmpty();
    }

    @Test
    void shouldReturnTheMappedPermissionWhenGettingById() {
        PermissionResponse response = new PermissionResponse();
        response.setName("ORDER_READ");

        when(identityClient.getPermission("1")).thenReturn(new ClientResponseWrapper(HttpStatus.OK, response));

        assertThat(permissionService.getById("1").getName()).isEqualTo("ORDER_READ");
    }
}
