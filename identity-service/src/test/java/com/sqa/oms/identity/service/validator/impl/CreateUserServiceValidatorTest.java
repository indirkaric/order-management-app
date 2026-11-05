package com.sqa.oms.identity.service.validator.impl;

import com.sqa.oms.identity.constants.Constants;
import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.exception.BusinessException;
import com.sqa.oms.identity.modules.user.model.CreateUserRequest;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.IUserRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserServiceValidatorTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IGroupRepository groupRepository;

    private CreateUserServiceValidator validator;

    @BeforeEach
    void setUp() {
        Validator beanValidator = Validation.buildDefaultValidatorFactory().getValidator();
        validator = new CreateUserServiceValidator(beanValidator, userRepository, groupRepository);
    }

    @Test
    void validateRejectsAlreadyTakenEmail() {
        CreateUserRequest request = new CreateUserRequest("John", "Doe", "john.doe@oms.local",
                List.of("GROUP0001"));
        when(userRepository.existsByEmail("john.doe@oms.local")).thenReturn(true);

        assertThatThrownBy(() -> validator.validate(request)).isInstanceOf(BusinessException.class);
    }

    @Test
    void validateRejectsEmptyGroups() {
        CreateUserRequest request = new CreateUserRequest("John", "Doe", "john.doe@oms.local", List.of());
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThatThrownBy(() -> validator.validate(request)).isInstanceOf(BusinessException.class);
    }

    @Test
    void validateRejectsUnknownGroupIds() {
        CreateUserRequest request = new CreateUserRequest("John", "Doe", "john.doe@oms.local",
                List.of("UNKNOWN-GROUP"));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(groupRepository.findAllByEntityIdIn(List.of("UNKNOWN-GROUP"))).thenReturn(List.of());

        assertThatThrownBy(() -> validator.validate(request)).isInstanceOf(BusinessException.class);
    }

    @Test
    void validateRejectsInactiveGroups() {
        CreateUserRequest request = new CreateUserRequest("John", "Doe", "john.doe@oms.local",
                List.of("GROUP0001"));
        Group inactiveGroup = new Group();
        inactiveGroup.setEntityId("GROUP0001");
        inactiveGroup.setStatus(Constants.Status.INACTIVE_STATUS);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(groupRepository.findAllByEntityIdIn(List.of("GROUP0001"))).thenReturn(List.of(inactiveGroup));

        assertThatThrownBy(() -> validator.validate(request)).isInstanceOf(BusinessException.class);
    }

    @Test
    void validateAcceptsAValidRequest() {
        CreateUserRequest request = new CreateUserRequest("John", "Doe", "john.doe@oms.local",
                List.of("GROUP0001"));
        Group activeGroup = new Group();
        activeGroup.setEntityId("GROUP0001");
        activeGroup.setStatus(Constants.Status.ACTIVE_STATUS);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(groupRepository.findAllByEntityIdIn(List.of("GROUP0001"))).thenReturn(List.of(activeGroup));

        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }
}
