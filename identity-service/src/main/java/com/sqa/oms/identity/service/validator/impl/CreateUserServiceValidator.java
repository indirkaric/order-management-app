package com.sqa.oms.identity.service.validator.impl;

import com.sqa.oms.identity.modules.user.model.CreateUserRequest;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.IUserRepository;
import com.sqa.oms.identity.service.validator.BaseUserDataValidator;
import com.sqa.oms.identity.service.validator.ICreateUserServiceValidator;
import com.sqa.oms.identity.util.Check;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Default implementation of the user create validation.
 */
@Service
public final class CreateUserServiceValidator extends BaseUserDataValidator implements ICreateUserServiceValidator {

    /**
     * Constructor with dependency injection.
     *
     * @param validator       bean validation engine
     * @param userRepository  access to the user table
     * @param groupRepository access to the group table
     */
    public CreateUserServiceValidator(final Validator validator,
                                      final IUserRepository userRepository,
                                      final IGroupRepository groupRepository) {
        super(validator, userRepository, groupRepository);
    }

    @Override
    public void validate(final CreateUserRequest request) {
        Check.isNotNull(request, "request");

        validateFields(request);
        validateEmail(request.getEmail());
        validateGroupIds(request.getGroupIds(), Optional.empty());
    }
}
