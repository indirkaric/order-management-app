package com.sqa.oms.ui.modules.identity;

import com.sqa.oms.ui.modules.identity.model.CreateGroupRequest;
import com.sqa.oms.ui.modules.identity.model.EntityCreatedResponse;
import com.sqa.oms.ui.modules.identity.model.GroupListRequest;
import com.sqa.oms.ui.modules.identity.model.GroupListResponse;
import com.sqa.oms.ui.modules.identity.model.GroupResponse;
import com.sqa.oms.ui.modules.identity.model.UpdateGroupRequest;
import com.sqa.oms.ui.service.identity.IGroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupControllerTest {

    private static final int PAGE_SIZE = 50;

    @Mock
    private IGroupService groupService;

    private GroupController groupController;

    @BeforeEach
    void setUp() {
        groupController = new GroupController(groupService);
    }

    @Test
    void shouldDelegateToTheServiceWhenGettingAll() {
        GroupListRequest request = new GroupListRequest(null, null, null, null);
        GroupListResponse response = new GroupListResponse(List.of(new GroupResponse()), 1);
        when(groupService.getAll(request, PageRequest.of(0, PAGE_SIZE))).thenReturn(response);

        assertThat(groupController.getAll(request, PageRequest.of(0, PAGE_SIZE)).getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturnNoContentWhenNoGroupsAreFound() {
        GroupListRequest request = new GroupListRequest(null, null, null, null);
        GroupListResponse response = new GroupListResponse(List.of(), 0);
        when(groupService.getAll(request, PageRequest.of(0, PAGE_SIZE))).thenReturn(response);

        assertThat(groupController.getAll(request, PageRequest.of(0, PAGE_SIZE)).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldDelegateToTheServiceWhenGettingById() {
        GroupResponse response = new GroupResponse();
        when(groupService.getById("1", null)).thenReturn(response);

        assertThat(groupController.getById("1", null).getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturn201OnCreate() {
        CreateGroupRequest request = new CreateGroupRequest("OMS_MANAGER", "desc", List.of("ORDER_READ"));
        EntityCreatedResponse response = new EntityCreatedResponse("GROUP0001");
        when(groupService.create(request)).thenReturn(response);

        assertThat(groupController.create(request).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void shouldReturn204OnUpdate() {
        UpdateGroupRequest request = new UpdateGroupRequest("OMS_MANAGER", "desc", (short) 1,
                List.of("ORDER_READ"));

        assertThat(groupController.update("1", request).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(groupService).update("1", request);
    }

    @Test
    void shouldReturn204OnDelete() {
        assertThat(groupController.delete("1").getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(groupService).delete("1");
    }
}
