package com.sqa.oms.identity.modules.user;

import com.sqa.oms.identity.dto.response.EntityCreatedResponse;
import com.sqa.oms.identity.enums.ExpandOption;
import com.sqa.oms.identity.modules.user.model.CreateUserRequest;
import com.sqa.oms.identity.modules.user.model.ModifyUserRequest;
import com.sqa.oms.identity.modules.user.model.UserListRequest;
import com.sqa.oms.identity.modules.user.model.UserListResponse;
import com.sqa.oms.identity.modules.user.model.UserResponse;
import com.sqa.oms.identity.modules.user.service.ICreateUserResponseCreatorService;
import com.sqa.oms.identity.modules.user.service.IUserListResponseCreatorService;
import com.sqa.oms.identity.modules.user.service.IUserResponseCreatorService;
import com.sqa.oms.identity.service.user.IModifyUserService;
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
    private ICreateUserResponseCreatorService createUserResponseCreatorService;

    @Mock
    private IUserResponseCreatorService userResponseCreatorService;

    @Mock
    private IUserListResponseCreatorService userListResponseCreatorService;

    @Mock
    private IModifyUserService modifyUserService;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(createUserResponseCreatorService, userResponseCreatorService,
                userListResponseCreatorService, modifyUserService);
    }

    @Test
    void getUsersDelegatesToTheResponseCreatorService() {
        UserListRequest request = new UserListRequest(null, null, null, null, null, null);
        UserListResponse response = new UserListResponse(List.of(new UserResponse()), 1);
        when(userListResponseCreatorService.create(request, PageRequest.of(0, PAGE_SIZE))).thenReturn(response);

        ResponseEntity<UserListResponse> result = userController.getUsers(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturnNoContentWhenNoUsersAreFound() {
        UserListRequest request = new UserListRequest(null, null, null, null, null, null);
        UserListResponse response = new UserListResponse(List.of(), 0);
        when(userListResponseCreatorService.create(request, PageRequest.of(0, PAGE_SIZE))).thenReturn(response);

        ResponseEntity<UserListResponse> result = userController.getUsers(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void getUserDelegatesToTheResponseCreatorService() {
        UserResponse response = new UserResponse();
        when(userResponseCreatorService.create("USR00001", ExpandOption.PERMISSIONS_GROUPS)).thenReturn(response);

        ResponseEntity<UserResponse> result = userController.getUser("usr00001", ExpandOption.PERMISSIONS_GROUPS);

        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void createReturns201WithTheCreatedUser() {
        CreateUserRequest request = new CreateUserRequest("Jane", "Doe", "jane.doe@oms.local", List.of());
        EntityCreatedResponse response = new EntityCreatedResponse("USR00001");
        when(createUserResponseCreatorService.create(request)).thenReturn(response);

        ResponseEntity<EntityCreatedResponse> result = userController.create(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void modifyReturns204AndDelegatesToTheService() {
        ModifyUserRequest request = new ModifyUserRequest("Jane", "Doe", "jane.doe@oms.local", (short) 1, List.of());

        ResponseEntity<Void> result = userController.modify("usr00001", request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(modifyUserService).update("USR00001", request);
    }
}
