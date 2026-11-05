package com.sqa.oms.identity.service.validator;

import com.sqa.oms.identity.constants.Constants;
import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.entity.Permission;
import com.sqa.oms.identity.exception.BusinessException;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.IPermissionRepository;
import com.sqa.oms.identity.util.Check;
import com.sqa.oms.identity.util.CollectionHelper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Shared validation rules of the group create and update flows.
 */
public abstract class BaseGroupDataValidator {

    private final Validator validator;

    private final IGroupRepository groupRepository;

    private final IPermissionRepository permissionRepository;

    /**
     * Constructor with dependency injection.
     *
     * @param validator           bean validation engine
     * @param groupRepository     access to the group table
     * @param permissionRepository access to the permission table
     */
    protected BaseGroupDataValidator(final Validator validator,
                                     final IGroupRepository groupRepository,
                                     final IPermissionRepository permissionRepository) {
        Check.isNotNull(validator, "validator");
        Check.isNotNull(groupRepository, "groupRepository");
        Check.isNotNull(permissionRepository, "permissionRepository");

        this.validator = validator;
        this.groupRepository = groupRepository;
        this.permissionRepository = permissionRepository;
    }

    /**
     * Runs the bean validation constraints declared on the request.
     *
     * @param <T>     type of the validated request
     * @param request request to validate
     */
    protected <T> void validateFields(final T request) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            List<String> messages = new ArrayList<>();
            for (ConstraintViolation<T> violation : violations) {
                messages.add(violation.getMessage());
            }

            throw new BusinessException(messages);
        }
    }

    /**
     * Checks that the group name is not already taken.
     *
     * @param name candidate group name
     */
    protected void validateName(final String name) {
        if (groupRepository.existsByName(name)) {
            throw new BusinessException(String.format("Group with the name %s already exists", name));
        }
    }

    /**
     * Checks that every requested permission id exists and is active.
     *
     * @param permissionIds requested permission ids
     */
    protected void validatePermissionIds(final List<String> permissionIds) {
        List<Permission> permissions = permissionRepository.findAllByEntityIdIn(permissionIds);

        List<String> missing = permissionIds.stream()
                .filter(permissionId -> permissions.stream()
                        .noneMatch(permission -> permission.getEntityId().equals(permissionId)))
                .toList();

        List<String> messages = new ArrayList<>();
        if (!missing.isEmpty()) {
            messages.add(String.format("Permissions with ids %s do not exist", missing));
        }

        List<String> inactive = permissions.stream()
                .filter(permission -> permission.getStatus().equals(Constants.Status.INACTIVE_STATUS))
                .map(Permission::getEntityId)
                .toList();

        if (!inactive.isEmpty()) {
            messages.add(String.format("Permissions with ids %s are inactive", inactive));
        }

        if (CollectionHelper.isNotEmpty(messages)) {
            throw new BusinessException(messages);
        }
    }

    /**
     * Blocks deactivating a group that still has members.
     *
     * @param group     group being modified
     * @param newStatus status requested by the update
     */
    protected void validateStatus(final Group group, final Short newStatus) {
        if (newStatus.equals(Constants.Status.INACTIVE_STATUS)
                && group.getStatus().equals(Constants.Status.ACTIVE_STATUS)
                && groupRepository.hasGroupUsers(group.getEntityId())) {
            throw new BusinessException(String.format(
                    "There are still users linked to group %s so it cannot be deactivated at this time",
                    group.getEntityId()));
        }
    }
}
