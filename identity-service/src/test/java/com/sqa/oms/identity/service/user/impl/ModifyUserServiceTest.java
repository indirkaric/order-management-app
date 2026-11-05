package com.sqa.oms.identity.service.user.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.entity.User;
import com.sqa.oms.identity.entity.UserGroup;
import com.sqa.oms.identity.entity.id.UserGroupId;
import com.sqa.oms.identity.exception.EntityNotFoundException;
import com.sqa.oms.identity.mapper.IUserMapper;
import com.sqa.oms.identity.modules.user.model.ModifyUserRequest;
import com.sqa.oms.identity.modules.user.model.UserResponse;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.IUserGroupRepository;
import com.sqa.oms.identity.repository.IUserRepository;
import com.sqa.oms.identity.security.ICurrentCallerProvider;
import com.sqa.oms.identity.service.validator.IModifyUserServiceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModifyUserServiceTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IGroupRepository groupRepository;

    @Mock
    private IUserGroupRepository userGroupRepository;

    @Mock
    private IUserMapper userMapper;

    @Mock
    private IModifyUserServiceValidator validator;

    @Mock
    private ICurrentCallerProvider currentCallerProvider;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private ModifyUserService modifyUserService;

    @BeforeEach
    void setUp() {
        modifyUserService = new ModifyUserService(userRepository, groupRepository, userGroupRepository,
                userMapper, validator, currentCallerProvider, applicationEventPublisher, new ObjectMapper());
    }

    @Test
    void updateThrowsWhenUserDoesNotExist() {
        ModifyUserRequest request = new ModifyUserRequest(
                "John", "Doe", "john.doe@oms.local", (short) 1, List.of());
        when(userRepository.findByEntityId("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> modifyUserService.update("UNKNOWN", request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateValidatesAgainstTheExistingUser() {
        User user = new User();
        user.setEntityId("USR00001");
        user.setEmail("john.doe@oms.local");

        ModifyUserRequest request = new ModifyUserRequest(
                "John", "Doe", "john.doe@oms.local", (short) 1, List.of());

        when(userRepository.findByEntityId("USR00001")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userGroupRepository.findByUser(user)).thenReturn(List.of());
        when(userMapper.toResponse(any(User.class))).thenReturn(new UserResponse());

        modifyUserService.update("USR00001", request);

        verify(validator).validate(user, request);
    }

    @Test
    void updateReplacesTheWholeGroupSet() {
        User user = new User();
        user.setEntityId("USR00001");
        user.setEmail("john.doe@oms.local");

        Group currentGroup = new Group();
        currentGroup.setEntityId("GROUP-OLD");
        UserGroup currentMembership = new UserGroup();
        currentMembership.setUserGroupId(new UserGroupId(user, currentGroup));

        Group newGroup = new Group();
        newGroup.setEntityId("GROUP-NEW");

        ModifyUserRequest request = new ModifyUserRequest(
                "John", "Doe", "john.doe@oms.local", (short) 1, List.of("GROUP-NEW"));

        when(userRepository.findByEntityId("USR00001")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userGroupRepository.findByUser(user)).thenReturn(List.of(currentMembership));
        when(groupRepository.findAllByEntityIdIn(List.of("GROUP-NEW"))).thenReturn(List.of(newGroup));
        when(userMapper.toResponse(any(User.class))).thenReturn(new UserResponse());

        modifyUserService.update("USR00001", request);

        ArgumentCaptor<List<UserGroup>> deletedCaptor = ArgumentCaptor.forClass(List.class);
        verify(userGroupRepository).deleteAll(deletedCaptor.capture());
        assertThat(deletedCaptor.getValue()).containsExactly(currentMembership);

        ArgumentCaptor<List<UserGroup>> savedCaptor = ArgumentCaptor.forClass(List.class);
        verify(userGroupRepository).saveAll(savedCaptor.capture());
        assertThat(savedCaptor.getValue()).hasSize(1);
        assertThat(savedCaptor.getValue().get(0).getUserGroupId().getGroup()).isEqualTo(newGroup);
    }
}
