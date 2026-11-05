package com.sqa.oms.ui.service.identity.impl;

import com.sqa.oms.ui.client.identity.IdentityClient;
import com.sqa.oms.ui.client.response.ClientResponseWrapper;
import com.sqa.oms.ui.exception.EntityNotFoundException;
import com.sqa.oms.ui.modules.identity.model.CreateUserRequest;
import com.sqa.oms.ui.modules.identity.model.EntityCreatedResponse;
import com.sqa.oms.ui.modules.identity.model.ModifyUserRequest;
import com.sqa.oms.ui.modules.identity.model.UserListRequest;
import com.sqa.oms.ui.modules.identity.model.UserListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final int PAGE_SIZE = 50;

    @Mock
    private IdentityClient identityClient;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(identityClient);
    }

    @Test
    void shouldForwardTheRequestAndReturnTheCreatedEntityIdOnCreate() {
        EntityCreatedResponse response = new EntityCreatedResponse("USR1");
        CreateUserRequest request = new CreateUserRequest("Jane", "Doe", "jane.doe@oms.local", List.of());

        when(identityClient.createUser(request)).thenReturn(new ClientResponseWrapper(HttpStatus.CREATED, response));

        EntityCreatedResponse result = userService.create(request);

        assertThat(result.id()).isEqualTo("USR1");
    }

    @Test
    void shouldForwardTheQueryParamsWhenGettingAll() {
        UserListResponse response = new UserListResponse(List.of(), 0);
        UserListRequest request = new UserListRequest(null, null, null, null, null, null);

        when(identityClient.getUsers(any(), any())).thenReturn(new ClientResponseWrapper(HttpStatus.OK, response));

        assertThat(userService.getAll(request, PageRequest.of(0, PAGE_SIZE))).isEqualTo(response);
    }

    @Test
    void shouldReturnAnEmptyResponseWhenTheIdentityServiceReturnsNoContentOnGettingAll() {
        UserListRequest request = new UserListRequest(null, null, null, null, null, null);

        when(identityClient.getUsers(any(), any())).thenReturn(new ClientResponseWrapper(HttpStatus.NO_CONTENT,
                null));

        assertThat(userService.getAll(request, PageRequest.of(0, PAGE_SIZE)).getData()).isEmpty();
    }

    @Test
    void shouldDelegateToTheClientOnUpdate() {
        ModifyUserRequest request = new ModifyUserRequest("Jane", "Doe", "jane.doe@oms.local", (short) 1, List.of());

        when(identityClient.updateUser("1", request)).thenReturn(new ClientResponseWrapper(HttpStatus.NO_CONTENT,
                null));

        userService.update("1", request);

        verify(identityClient).updateUser("1", request);
    }

    @Test
    void shouldDelegateToTheClientOnDelete() {
        when(identityClient.deleteUser("1")).thenReturn(new ClientResponseWrapper(HttpStatus.NO_CONTENT, null));

        userService.delete("1");

        verify(identityClient).deleteUser("1");
    }

    @Test
    void shouldThrowNotFoundWhenTheIdentityServiceReturns404OnGetById() {
        when(identityClient.getUser(anyString(), any()))
                .thenReturn(new ClientResponseWrapper(HttpStatus.NOT_FOUND, null));

        assertThatThrownBy(() -> userService.getById("unknown", null)).isInstanceOf(EntityNotFoundException.class);
    }
}
