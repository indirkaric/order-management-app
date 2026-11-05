package com.sqa.oms.identity.modules.group;

import com.sqa.oms.identity.dto.response.EntityCreatedResponse;
import com.sqa.oms.identity.enums.ExpandOption;
import com.sqa.oms.identity.modules.group.model.CreateGroupRequest;
import com.sqa.oms.identity.modules.group.model.GroupListRequest;
import com.sqa.oms.identity.modules.group.model.GroupListResponse;
import com.sqa.oms.identity.modules.group.model.GroupResponse;
import com.sqa.oms.identity.modules.group.model.UpdateGroupRequest;
import com.sqa.oms.identity.modules.group.service.ICreateGroupResponseCreatorService;
import com.sqa.oms.identity.modules.group.service.IGroupListResponseCreatorService;
import com.sqa.oms.identity.modules.group.service.IGroupResponseCreatorService;
import com.sqa.oms.identity.service.group.IModifyGroupService;
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
class GroupControllerTest {

    private static final int PAGE_SIZE = 50;

    @Mock
    private ICreateGroupResponseCreatorService createGroupResponseCreatorService;

    @Mock
    private IGroupResponseCreatorService groupResponseCreatorService;

    @Mock
    private IGroupListResponseCreatorService groupListResponseCreatorService;

    @Mock
    private IModifyGroupService modifyGroupService;

    private GroupController groupController;

    @BeforeEach
    void setUp() {
        groupController = new GroupController(createGroupResponseCreatorService, groupResponseCreatorService,
                groupListResponseCreatorService, modifyGroupService);
    }

    @Test
    void getGroupsDelegatesToTheResponseCreatorService() {
        GroupListRequest request = new GroupListRequest(null, null, null, null);
        GroupListResponse response = new GroupListResponse(List.of(new GroupResponse()), 1);
        when(groupListResponseCreatorService.create(request, PageRequest.of(0, PAGE_SIZE))).thenReturn(response);

        ResponseEntity<GroupListResponse> result = groupController.getGroups(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturnNoContentWhenNoGroupsAreFound() {
        GroupListRequest request = new GroupListRequest(null, null, null, null);
        GroupListResponse response = new GroupListResponse(List.of(), 0);
        when(groupListResponseCreatorService.create(request, PageRequest.of(0, PAGE_SIZE))).thenReturn(response);

        ResponseEntity<GroupListResponse> result = groupController.getGroups(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void getGroupDelegatesToTheResponseCreatorService() {
        GroupResponse response = new GroupResponse();
        when(groupResponseCreatorService.create("GROUP0001", ExpandOption.PERMISSIONS)).thenReturn(response);

        ResponseEntity<GroupResponse> result = groupController.getGroup("group0001", ExpandOption.PERMISSIONS);

        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void createReturns201WithTheCreatedGroup() {
        CreateGroupRequest request = new CreateGroupRequest("OMS_MANAGER", "desc", List.of("ORDER_READ"));
        EntityCreatedResponse response = new EntityCreatedResponse("GROUP0001");
        when(createGroupResponseCreatorService.create(request)).thenReturn(response);

        ResponseEntity<EntityCreatedResponse> result = groupController.create(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void updateReturns204AndDelegatesToTheService() {
        UpdateGroupRequest request = new UpdateGroupRequest("OMS_MANAGER", "desc", (short) 1, List.of("ORDER_READ"));

        ResponseEntity<Void> result = groupController.update("group0001", request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(modifyGroupService).update("GROUP0001", request);
    }
}
