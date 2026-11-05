package com.sqa.oms.identity.service.validator;

import com.sqa.oms.identity.constants.Constants;
import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.exception.BusinessException;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.IUserRepository;
import com.sqa.oms.identity.util.Check;
import com.sqa.oms.identity.util.CollectionHelper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Shared validation rules of the user create and update flows.
 */
public abstract class BaseUserDataValidator {

    private final Validator validator;

    private final IUserRepository userRepository;

    private final IGroupRepository groupRepository;

    /**
     * Constructor with dependency injection.
     *
     * @param validator      bean validation engine
     * @param userRepository access to the user table
     * @param groupRepository access to the group table
     */
    protected BaseUserDataValidator(final Validator validator,
                                    final IUserRepository userRepository,
                                    final IGroupRepository groupRepository) {
        Check.isNotNull(validator, "validator");
        Check.isNotNull(userRepository, "userRepository");
        Check.isNotNull(groupRepository, "groupRepository");

        this.validator = validator;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
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
     * Checks that the email is not already taken.
     *
     * @param email candidate email
     */
    protected void validateEmail(final String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(String.format("Email %s is already taken", email));
        }
    }

    /**
     * Checks that the requested group ids exist and are active, and that at least one group is
     * requested unless the user is being deactivated.
     *
     * @param groupIds        requested group ids
     * @param requestedStatus status requested by the update, empty on create
     */
    protected void validateGroupIds(final List<String> groupIds, final Optional<Short> requestedStatus) {
        boolean isDeactivating = requestedStatus.isPresent()
                && requestedStatus.get().equals(Constants.Status.INACTIVE_STATUS);

        if (!CollectionHelper.isNotEmpty(groupIds) && !isDeactivating) {
            throw new BusinessException("Permission groups can not be empty");
        }

        if (!CollectionHelper.isNotEmpty(groupIds)) {
            return;
        }

        List<Group> groups = groupRepository.findAllByEntityIdIn(groupIds);

        List<String> missing = groupIds.stream()
                .filter(groupId -> groups.stream().noneMatch(group -> group.getEntityId().equals(groupId)))
                .toList();

        List<String> messages = new ArrayList<>();
        if (!missing.isEmpty()) {
            messages.add(String.format("Groups with ids %s do not exist", missing));
        }

        List<String> inactive = groups.stream()
                .filter(group -> group.getStatus().equals(Constants.Status.INACTIVE_STATUS))
                .map(Group::getEntityId)
                .toList();

        if (!inactive.isEmpty()) {
            messages.add(String.format("Groups with ids %s are inactive", inactive));
        }

        if (CollectionHelper.isNotEmpty(messages)) {
            throw new BusinessException(messages);
        }
    }
}
