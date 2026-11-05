package com.sqa.oms.ui.service.identity.impl;

import com.sqa.oms.ui.client.identity.IdentityClient;
import com.sqa.oms.ui.client.response.ClientResponseWrapper;
import com.sqa.oms.ui.modules.identity.model.CreateGroupRequest;
import com.sqa.oms.ui.modules.identity.model.EntityCreatedResponse;
import com.sqa.oms.ui.modules.identity.model.GroupListRequest;
import com.sqa.oms.ui.modules.identity.model.GroupListResponse;
import com.sqa.oms.ui.modules.identity.model.GroupResponse;
import com.sqa.oms.ui.modules.identity.model.UpdateGroupRequest;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    private static final int PAGE_SIZE = 50;

    @Mock
    private IdentityClient identityClient;

    private GroupService groupService;

    @BeforeEach
    void setUp() {
        groupService = new GroupService(identityClient);
    }

    @Test
    void shouldReturnTheMappedGroupWhenGettingById() {
        GroupResponse response = new GroupResponse();
        response.setName("OMS_MANAGER");

        when(identityClient.getGroup(eq("1"), any())).thenReturn(new ClientResponseWrapper(HttpStatus.OK, response));

        assertThat(groupService.getById("1", null).getName()).isEqualTo("OMS_MANAGER");
    }

    @Test
    void shouldForwardTheQueryParamsWhenGettingAll() {
        GroupListResponse response = new GroupListResponse(List.of(), 0);
        GroupListRequest request = new GroupListRequest(null, null, null, null);

        when(identityClient.getGroups(any(), any())).thenReturn(new ClientResponseWrapper(HttpStatus.OK, response));

        assertThat(groupService.getAll(request, PageRequest.of(0, PAGE_SIZE))).isEqualTo(response);
    }

    @Test
    void shouldReturnAnEmptyResponseWhenTheIdentityServiceReturnsNoContentOnGettingAll() {
        GroupListRequest request = new GroupListRequest(null, null, null, null);

        when(identityClient.getGroups(any(), any())).thenReturn(new ClientResponseWrapper(HttpStatus.NO_CONTENT,
                null));

        assertThat(groupService.getAll(request, PageRequest.of(0, PAGE_SIZE)).getData()).isEmpty();
    }

    @Test
    void shouldForwardTheRequestOnCreate() {
        EntityCreatedResponse response = new EntityCreatedResponse("GROUP0001");
        CreateGroupRequest request = new CreateGroupRequest("OMS_MANAGER", "desc", List.of("ORDER_READ"));

        when(identityClient.createGroup(request)).thenReturn(new ClientResponseWrapper(HttpStatus.CREATED,
                response));

        assertThat(groupService.create(request)).isEqualTo(response);
    }

    @Test
    void shouldDelegateToTheClientOnDelete() {
        when(identityClient.deleteGroup("1")).thenReturn(new ClientResponseWrapper(HttpStatus.NO_CONTENT, null));

        groupService.delete("1");

        verify(identityClient).deleteGroup("1");
    }

    @Test
    void shouldDelegateToTheClientOnUpdate() {
        UpdateGroupRequest request = new UpdateGroupRequest("OMS_MANAGER", "desc", (short) 1,
                List.of("ORDER_READ"));

        when(identityClient.updateGroup("1", request))
                .thenReturn(new ClientResponseWrapper(HttpStatus.NO_CONTENT, null));

        groupService.update("1", request);

        verify(identityClient).updateGroup("1", request);
    }
}
