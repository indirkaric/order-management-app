package com.sqa.oms.identity.service.user.impl;

import com.sqa.oms.identity.dto.UserDTO;
import com.sqa.oms.identity.exception.EntityNotFoundException;
import com.sqa.oms.identity.mapper.IUserMapper;
import com.sqa.oms.identity.modules.user.model.UserListRequest;
import com.sqa.oms.identity.repository.IUserRepository;
import com.sqa.oms.identity.repository.projection.UserProjection;
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
class UserServiceTest {

    private static final int PAGE_SIZE = 50;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IUserMapper userMapper;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userMapper);
    }

    @Test
    void getByEntityIdReturnsTheMatchingUser() {
        UserProjection projection = new UserProjection("USR00001", "Jane", "Doe", "jane@example.com", (short) 1);
        UserDTO dto = new UserDTO();
        when(userRepository.findProjectionByEntityId("USR00001")).thenReturn(Optional.of(projection));
        when(userMapper.toDto(projection)).thenReturn(dto);

        assertThat(userService.getByEntityId("USR00001")).isEqualTo(dto);
    }

    @Test
    void getByEntityIdThrowsWhenNotFound() {
        when(userRepository.findProjectionByEntityId("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByEntityId("UNKNOWN"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getPagedListDelegatesToTheRepository() {
        UserListRequest request = new UserListRequest(null, null, null, null, null, null);
        UserProjection projection = new UserProjection("USR00001", "Jane", "Doe", "jane@example.com", (short) 1);
        UserDTO dto = new UserDTO();
        Page<UserProjection> page = new PageImpl<>(List.of(projection));
        when(userRepository.findPagedList(request, PageRequest.of(0, PAGE_SIZE))).thenReturn(page);
        when(userMapper.toDto(projection)).thenReturn(dto);

        PagedList<UserDTO> result = userService.getPagedList(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getItems()).containsExactly(dto);
        assertThat(result.getCount()).isEqualTo(1);
    }
}
