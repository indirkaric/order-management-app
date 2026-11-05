package com.sqa.oms.ui.modules.identity;

import com.sqa.oms.ui.modules.identity.model.CreateUserRequest;
import com.sqa.oms.ui.modules.identity.model.EntityCreatedResponse;
import com.sqa.oms.ui.modules.identity.model.ModifyUserRequest;
import com.sqa.oms.ui.modules.identity.model.UserListRequest;
import com.sqa.oms.ui.modules.identity.model.UserListResponse;
import com.sqa.oms.ui.modules.identity.model.UserResponse;
import com.sqa.oms.ui.service.identity.IUserService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final int PAGE_SIZE = 50;

    @Mock
    private IUserService userService;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService);
    }

    @Test
    void shouldDelegateToTheServiceWhenGettingAll() {
        UserListRequest request = new UserListRequest(null, null, null, null, null, null);
        UserListResponse response = new UserListResponse(List.of(new UserResponse()), 1);
        when(userService.getAll(request, PageRequest.of(0, PAGE_SIZE))).thenReturn(response);

        ResponseEntity<UserListResponse> result = userController.getAll(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturnNoContentWhenNoUsersAreFound() {
        UserListRequest request = new UserListRequest(null, null, null, null, null, null);
        UserListResponse response = new UserListResponse(List.of(), 0);
        when(userService.getAll(request, PageRequest.of(0, PAGE_SIZE))).thenReturn(response);

        ResponseEntity<UserListResponse> result = userController.getAll(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldReturn201OnCreate() {
        CreateUserRequest request = new CreateUserRequest("Jane", "Doe", "jane.doe@oms.local", List.of());
        EntityCreatedResponse response = new EntityCreatedResponse("USR1");
        when(userService.create(request)).thenReturn(response);

        ResponseEntity<EntityCreatedResponse> result = userController.create(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturn204AndDelegateToTheServiceOnUpdate() {
        ModifyUserRequest request = new ModifyUserRequest("Jane", "Doe", "jane.doe@oms.local", (short) 1, List.of());

        ResponseEntity<Void> result = userController.update("USR1", request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService).update("USR1", request);
    }

    @Test
    void shouldReturn204OnDelete() {
        assertThat(userController.delete("1").getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService).delete("1");
    }
}
