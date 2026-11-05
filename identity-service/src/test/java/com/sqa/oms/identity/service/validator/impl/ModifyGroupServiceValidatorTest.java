package com.sqa.oms.identity.service.validator.impl;

import com.sqa.oms.identity.constants.Constants;
import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.entity.Permission;
import com.sqa.oms.identity.exception.BusinessException;
import com.sqa.oms.identity.modules.group.model.UpdateGroupRequest;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModifyGroupServiceValidatorTest {

    @Mock
    private IGroupRepository groupRepository;

    @Mock
    private IPermissionRepository permissionRepository;

    private ModifyGroupServiceValidator validator;

    @BeforeEach
    void setUp() {
        Validator beanValidator = Validation.buildDefaultValidatorFactory().getValidator();
        validator = new ModifyGroupServiceValidator(beanValidator, groupRepository, permissionRepository);
    }

    @Test
    void validateBlocksDeactivationWhenGroupStillHasUsers() {
        Group group = new Group();
        group.setEntityId("GROUP0001");
        group.setName("OMS_MANAGER");
        group.setStatus(Constants.Status.ACTIVE_STATUS);

        UpdateGroupRequest request = new UpdateGroupRequest(
                "OMS_MANAGER", "desc", Constants.Status.INACTIVE_STATUS, List.of("ORDER_READ"));

        when(groupRepository.hasGroupUsers("GROUP0001")).thenReturn(true);

        assertThatThrownBy(() -> validator.validate(group, request)).isInstanceOf(BusinessException.class);
    }

    @Test
    void validateAllowsDeactivationWhenGroupHasNoUsers() {
        Group group = new Group();
        group.setEntityId("GROUP0001");
        group.setName("OMS_MANAGER");
        group.setStatus(Constants.Status.ACTIVE_STATUS);

        UpdateGroupRequest request = new UpdateGroupRequest(
                "OMS_MANAGER", "desc", Constants.Status.INACTIVE_STATUS, List.of("ORDER_READ"));

        Permission active = new Permission();
        active.setEntityId("ORDER_READ");
        active.setStatus(Constants.Status.ACTIVE_STATUS);
        when(permissionRepository.findAllByEntityIdIn(List.of("ORDER_READ"))).thenReturn(List.of(active));
        when(groupRepository.hasGroupUsers("GROUP0001")).thenReturn(false);

        assertThatCode(() -> validator.validate(group, request)).doesNotThrowAnyException();
    }

    @Test
    void validateSkipsNameCheckWhenNameIsUnchanged() {
        Group group = new Group();
        group.setEntityId("GROUP0001");
        group.setName("OMS_MANAGER");
        group.setStatus(Constants.Status.ACTIVE_STATUS);

        UpdateGroupRequest request = new UpdateGroupRequest(
                "OMS_MANAGER", "desc", Constants.Status.ACTIVE_STATUS, List.of("ORDER_READ"));

        Permission active = new Permission();
        active.setEntityId("ORDER_READ");
        active.setStatus(Constants.Status.ACTIVE_STATUS);
        when(permissionRepository.findAllByEntityIdIn(List.of("ORDER_READ"))).thenReturn(List.of(active));

        assertThatCode(() -> validator.validate(group, request)).doesNotThrowAnyException();
    }
}
