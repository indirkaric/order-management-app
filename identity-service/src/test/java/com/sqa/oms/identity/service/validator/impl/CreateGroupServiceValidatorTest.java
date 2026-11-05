package com.sqa.oms.identity.service.validator.impl;

import com.sqa.oms.identity.constants.Constants;
import com.sqa.oms.identity.entity.Permission;
import com.sqa.oms.identity.exception.BusinessException;
import com.sqa.oms.identity.modules.group.model.CreateGroupRequest;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.IPermissionRepository;
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
class CreateGroupServiceValidatorTest {

    @Mock
    private IGroupRepository groupRepository;

    @Mock
    private IPermissionRepository permissionRepository;

    private CreateGroupServiceValidator validator;

    @BeforeEach
    void setUp() {
        Validator beanValidator = Validation.buildDefaultValidatorFactory().getValidator();
        validator = new CreateGroupServiceValidator(beanValidator, groupRepository, permissionRepository);
    }

    @Test
    void validateRejectsAlreadyTakenName() {
        CreateGroupRequest request = new CreateGroupRequest("OMS_MANAGER", "desc", List.of("ORDER_READ"));
        when(groupRepository.existsByName("OMS_MANAGER")).thenReturn(true);

        assertThatThrownBy(() -> validator.validate(request)).isInstanceOf(BusinessException.class);
    }

    @Test
    void validateRejectsInactivePermissions() {
        CreateGroupRequest request = new CreateGroupRequest("OMS_MANAGER", "desc", List.of("ORDER_READ"));
        Permission inactive = new Permission();
        inactive.setEntityId("ORDER_READ");
        inactive.setStatus(Constants.Status.INACTIVE_STATUS);

        when(groupRepository.existsByName(anyString())).thenReturn(false);
        when(permissionRepository.findAllByEntityIdIn(List.of("ORDER_READ"))).thenReturn(List.of(inactive));

        assertThatThrownBy(() -> validator.validate(request)).isInstanceOf(BusinessException.class);
    }

    @Test
    void validateAcceptsAValidRequest() {
        CreateGroupRequest request = new CreateGroupRequest("OMS_MANAGER", "desc", List.of("ORDER_READ"));
        Permission active = new Permission();
        active.setEntityId("ORDER_READ");
        active.setStatus(Constants.Status.ACTIVE_STATUS);

        when(groupRepository.existsByName(anyString())).thenReturn(false);
        when(permissionRepository.findAllByEntityIdIn(List.of("ORDER_READ"))).thenReturn(List.of(active));

        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }
}
