package com.sqa.oms.identity.service.validator.impl;

import com.sqa.oms.identity.modules.group.model.CreateGroupRequest;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.IPermissionRepository;
import com.sqa.oms.identity.service.validator.BaseGroupDataValidator;
import com.sqa.oms.identity.service.validator.ICreateGroupServiceValidator;
import com.sqa.oms.identity.util.Check;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the group create validation.
 */
@Service
public final class CreateGroupServiceValidator extends BaseGroupDataValidator
        implements ICreateGroupServiceValidator {

    /**
     * Constructor with dependency injection.
     *
     * @param validator            bean validation engine
     * @param groupRepository      access to the group table
     * @param permissionRepository access to the permission table
     */
    public CreateGroupServiceValidator(final Validator validator,
                                       final IGroupRepository groupRepository,
                                       final IPermissionRepository permissionRepository) {
        super(validator, groupRepository, permissionRepository);
    }

    @Override
    public void validate(final CreateGroupRequest request) {
        Check.isNotNull(request, "request");

        validateFields(request);
        validateName(request.getName());
        validatePermissionIds(request.getPermissionIds());
    }
}
