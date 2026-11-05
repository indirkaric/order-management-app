package com.sqa.oms.identity.service.validator.impl;

import com.sqa.oms.identity.constants.Constants;
import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.entity.User;
import com.sqa.oms.identity.exception.BusinessException;
import com.sqa.oms.identity.modules.user.model.ModifyUserRequest;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModifyUserServiceValidatorTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IGroupRepository groupRepository;

    private ModifyUserServiceValidator validator;

    @BeforeEach
    void setUp() {
        Validator beanValidator = Validation.buildDefaultValidatorFactory().getValidator();
        validator = new ModifyUserServiceValidator(beanValidator, userRepository, groupRepository);
    }

    @Test
    void validateSkipsEmailCheckWhenEmailIsUnchanged() {
        User user = new User();
        user.setEmail("john.doe@oms.local");
        ModifyUserRequest request = new ModifyUserRequest(
                "John", "Doe", "john.doe@oms.local", Constants.Status.ACTIVE_STATUS, List.of("GROUP0001"));
        Group activeGroup = new Group();
        activeGroup.setEntityId("GROUP0001");
        activeGroup.setStatus(Constants.Status.ACTIVE_STATUS);
        when(groupRepository.findAllByEntityIdIn(List.of("GROUP0001"))).thenReturn(List.of(activeGroup));

        assertThatCode(() -> validator.validate(user, request)).doesNotThrowAnyException();
    }

    @Test
    void validateRejectsAlreadyTakenEmailWhenChanged() {
        User user = new User();
        user.setEmail("old@oms.local");
        ModifyUserRequest request = new ModifyUserRequest(
                "John", "Doe", "new@oms.local", Constants.Status.ACTIVE_STATUS, List.of("GROUP0001"));
        when(userRepository.existsByEmail("new@oms.local")).thenReturn(true);

        assertThatThrownBy(() -> validator.validate(user, request)).isInstanceOf(BusinessException.class);
    }

    @Test
    void validateAllowsEmptyGroupsWhenDeactivating() {
        User user = new User();
        user.setEmail("john.doe@oms.local");
        ModifyUserRequest request = new ModifyUserRequest(
                "John", "Doe", "john.doe@oms.local", Constants.Status.INACTIVE_STATUS, List.of());

        assertThatCode(() -> validator.validate(user, request)).doesNotThrowAnyException();
    }

    @Test
    void validateRejectsEmptyGroupsWhenStayingActive() {
        User user = new User();
        user.setEmail("john.doe@oms.local");
        ModifyUserRequest request = new ModifyUserRequest(
                "John", "Doe", "john.doe@oms.local", Constants.Status.ACTIVE_STATUS, List.of());

        assertThatThrownBy(() -> validator.validate(user, request)).isInstanceOf(BusinessException.class);
    }
}
