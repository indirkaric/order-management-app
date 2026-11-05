package com.sqa.oms.identity.modules.user.service.impl;

import com.sqa.oms.identity.dto.UserDTO;
import com.sqa.oms.identity.mapper.IGroupMapper;
import com.sqa.oms.identity.mapper.IUserMapper;
import com.sqa.oms.identity.modules.user.model.UserListRequest;
import com.sqa.oms.identity.modules.user.model.UserListResponse;
import com.sqa.oms.identity.modules.user.model.UserResponse;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.service.model.PagedList;
import com.sqa.oms.identity.service.user.IUserService;
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
class UserListResponseCreatorServiceTest {

    private static final int PAGE_SIZE = 50;

    @Mock
    private IUserService userService;

    @Mock
    private IUserMapper userMapper;

    @Mock
    private IGroupMapper groupMapper;

    @Mock
    private IGroupRepository groupRepository;

    private UserListResponseCreatorService responseCreatorService;

    @BeforeEach
    void setUp() {
        responseCreatorService =
                new UserListResponseCreatorService(userService, userMapper, groupMapper, groupRepository);
    }

    @Test
    void createMapsThePageIntoAListResponse() {
        UserDTO user = new UserDTO();
        user.setEntityId("USR00001");
        UserListRequest request = new UserListRequest(null, null, null, null, null, null);

        when(userService.getPagedList(request, PageRequest.of(0, PAGE_SIZE)))
                .thenReturn(new PagedList<>(List.of(user), 1));
        when(userMapper.toResponse(user)).thenReturn(new UserResponse());

        UserListResponse response = responseCreatorService.create(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(response.getData()).hasSize(1);
        assertThat(response.getCount()).isEqualTo(1);
    }
}
