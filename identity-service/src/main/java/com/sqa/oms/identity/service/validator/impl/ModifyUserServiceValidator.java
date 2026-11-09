package com.sqa.oms.identity.service.validator.impl;

import com.sqa.oms.identity.entity.User;
import com.sqa.oms.identity.modules.user.model.ModifyUserRequest;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.IUserRepository;
import com.sqa.oms.identity.service.validator.BaseUserDataValidator;
import com.sqa.oms.identity.service.validator.IModifyUserServiceValidator;
import com.sqa.oms.identity.util.Check;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Default implementation of the user update validation.
 */
@Service
public final class ModifyUserServiceValidator extends BaseUserDataValidator implements IModifyUserServiceValidator {

    /**
     * Constructor with dependency injection.
     *
     * @param validator       bean validation engine
     * @param userRepository  access to the user table
     * @param groupRepository access to the group table
     */
    public ModifyUserServiceValidator(final Validator validator,
                                      final IUserRepository userRepository,
                                      final IGroupRepository groupRepository) {
        super(validator, userRepository, groupRepository);
    }

    @Override
    public void validate(final User user, final ModifyUserRequest request) {
        Check.isNotNull(user, "user");
        Check.isNotNull(request, "request");

        validateFields(request);

        if (!request.getEmail().equals(user.getEmail())) {
            validateEmail(request.getEmail());
        }

        validateGroupIds(request.getGroupIds(), Optional.of(request.getStatus()));
    }
}
