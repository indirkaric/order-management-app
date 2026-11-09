package com.sqa.oms.identity.service.validator.impl;

import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.modules.group.model.UpdateGroupRequest;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.IPermissionRepository;
import com.sqa.oms.identity.service.validator.BaseGroupDataValidator;
import com.sqa.oms.identity.service.validator.IModifyGroupServiceValidator;
import com.sqa.oms.identity.util.Check;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the group update validation.
 */
@Service
public final class ModifyGroupServiceValidator extends BaseGroupDataValidator
        implements IModifyGroupServiceValidator {

    /**
     * Constructor with dependency injection.
     *
     * @param validator            bean validation engine
     * @param groupRepository      access to the group table
     * @param permissionRepository access to the permission table
     */
    public ModifyGroupServiceValidator(final Validator validator,
                                       final IGroupRepository groupRepository,
                                       final IPermissionRepository permissionRepository) {
        super(validator, groupRepository, permissionRepository);
    }

    @Override
    public void validate(final Group group, final UpdateGroupRequest request) {
        Check.isNotNull(group, "group");
        Check.isNotNull(request, "request");

        validateFields(request);

        if (!group.getName().equalsIgnoreCase(request.getName())) {
            validateName(request.getName());
        }

        validateStatus(group, request.getStatus());
        validatePermissionIds(request.getPermissionIds());
    }
}
