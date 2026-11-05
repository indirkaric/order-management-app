package com.sqa.oms.identity.modules.group.service.impl;

import com.sqa.oms.identity.dto.GroupDTO;
import com.sqa.oms.identity.mapper.IGroupMapper;
import com.sqa.oms.identity.modules.group.model.GroupListRequest;
import com.sqa.oms.identity.modules.group.model.GroupListResponse;
import com.sqa.oms.identity.modules.group.model.GroupResponse;
import com.sqa.oms.identity.repository.IPermissionRepository;
import com.sqa.oms.identity.service.group.IGroupService;
import com.sqa.oms.identity.service.model.PagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupListResponseCreatorServiceTest {

    private static final int PAGE_SIZE = 50;

    @Mock
    private IGroupService groupService;

    @Mock
    private IGroupMapper groupMapper;

    @Mock
    private IPermissionRepository permissionRepository;

    private GroupListResponseCreatorService responseCreatorService;

    @BeforeEach
    void setUp() {
        responseCreatorService =
                new GroupListResponseCreatorService(groupService, groupMapper, permissionRepository);
    }

    @Test
    void createMapsThePageIntoAListResponse() {
        GroupDTO group = new GroupDTO();
        group.setEntityId("GROUP0001");
        GroupListRequest request = new GroupListRequest(null, null, null, null);

        when(groupService.getPagedList(request, PageRequest.of(0, PAGE_SIZE)))
                .thenReturn(new PagedList<>(List.of(group), 1));
        when(groupMapper.toResponse(group)).thenReturn(new GroupResponse());

        GroupListResponse response = responseCreatorService.create(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(response.getData()).hasSize(1);
        assertThat(response.getCount()).isEqualTo(1);
    }
}
