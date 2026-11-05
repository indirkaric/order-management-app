package com.sqa.oms.identity.service.user.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqa.oms.identity.entity.User;
import com.sqa.oms.identity.event.DomainChangeEvent;
import com.sqa.oms.identity.mapper.IUserMapper;
import com.sqa.oms.identity.modules.user.model.CreateUserRequest;
import com.sqa.oms.identity.modules.user.model.UserResponse;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.IUserGroupRepository;
import com.sqa.oms.identity.repository.IUserRepository;
import com.sqa.oms.identity.security.ICurrentCallerProvider;
import com.sqa.oms.identity.service.validator.ICreateUserServiceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IGroupRepository groupRepository;

    @Mock
    private IUserGroupRepository userGroupRepository;

    @Mock
    private IUserMapper userMapper;

    @Mock
    private ICreateUserServiceValidator validator;

    @Mock
    private ICurrentCallerProvider currentCallerProvider;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private CreateUserService createUserService;

    @BeforeEach
    void setUp() {
        createUserService = new CreateUserService(userRepository, groupRepository, userGroupRepository,
                userMapper, validator, currentCallerProvider, applicationEventPublisher, new ObjectMapper());
    }

    @Test
    void saveValidatesBeforePersisting() {
        CreateUserRequest request = new CreateUserRequest("John", "Doe", "john.doe@oms.local", List.of());
        when(userRepository.existsByEntityId(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenReturn(new UserResponse());

        createUserService.save(request);

        verify(validator).validate(request);
        verify(userRepository).save(any(User.class));
        verify(userGroupRepository, never()).saveAll(any());
        verify(applicationEventPublisher).publishEvent(any(DomainChangeEvent.class));
    }

    @Test
    void saveAssignsEveryRequestedGroup() {
        CreateUserRequest request = new CreateUserRequest("John", "Doe", "john.doe@oms.local",
                List.of("GROUP0001"));
        com.sqa.oms.identity.entity.Group group = new com.sqa.oms.identity.entity.Group();
        group.setEntityId("GROUP0001");

        when(userRepository.existsByEntityId(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(groupRepository.findAllByEntityIdIn(List.of("GROUP0001"))).thenReturn(List.of(group));
        when(userMapper.toResponse(any(User.class))).thenReturn(new UserResponse());

        String entityId = createUserService.save(request);

        assertThat(entityId).isNotBlank();
        verify(userGroupRepository).saveAll(any());
    }
}
