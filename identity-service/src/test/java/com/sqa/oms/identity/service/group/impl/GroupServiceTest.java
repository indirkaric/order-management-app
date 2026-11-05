package com.sqa.oms.identity.service.group.impl;

import com.sqa.oms.identity.dto.GroupDTO;
import com.sqa.oms.identity.exception.EntityNotFoundException;
import com.sqa.oms.identity.mapper.IGroupMapper;
import com.sqa.oms.identity.modules.group.model.GroupListRequest;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.projection.GroupProjection;
import com.sqa.oms.identity.service.model.PagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    private static final int PAGE_SIZE = 50;

    @Mock
    private IGroupRepository groupRepository;

    @Mock
    private IGroupMapper groupMapper;

    private GroupService groupService;

    @BeforeEach
    void setUp() {
        groupService = new GroupService(groupRepository, groupMapper);
    }

    @Test
    void getByEntityIdReturnsTheMatchingGroup() {
        GroupProjection projection = new GroupProjection("GROUP0001", "Group", null, (short) 1);
        GroupDTO dto = new GroupDTO();
        when(groupRepository.findProjectionByEntityId("GROUP0001")).thenReturn(Optional.of(projection));
        when(groupMapper.toDto(projection)).thenReturn(dto);

        assertThat(groupService.getByEntityId("GROUP0001")).isEqualTo(dto);
    }

    @Test
    void getByEntityIdThrowsWhenNotFound() {
        when(groupRepository.findProjectionByEntityId("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.getByEntityId("UNKNOWN"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getPagedListDelegatesToTheRepository() {
        GroupListRequest request = new GroupListRequest(null, null, null, null);
        GroupProjection projection = new GroupProjection("GROUP0001", "Group", null, (short) 1);
        GroupDTO dto = new GroupDTO();
        Page<GroupProjection> page = new PageImpl<>(List.of(projection));
        when(groupRepository.findPagedList(request, PageRequest.of(0, PAGE_SIZE))).thenReturn(page);
        when(groupMapper.toDto(projection)).thenReturn(dto);

        PagedList<GroupDTO> result = groupService.getPagedList(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getItems()).containsExactly(dto);
        assertThat(result.getCount()).isEqualTo(1);
    }
}
