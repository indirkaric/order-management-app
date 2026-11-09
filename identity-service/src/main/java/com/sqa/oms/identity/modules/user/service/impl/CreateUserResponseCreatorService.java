package com.sqa.oms.identity.modules.user.service.impl;

import com.sqa.oms.identity.dto.response.EntityCreatedResponse;
import com.sqa.oms.identity.modules.user.model.CreateUserRequest;
import com.sqa.oms.identity.modules.user.service.ICreateUserResponseCreatorService;
import com.sqa.oms.identity.service.user.ICreateUserService;
import com.sqa.oms.identity.util.Check;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the user create response builder.
 */
@Service
public final class CreateUserResponseCreatorService implements ICreateUserResponseCreatorService {

    private final ICreateUserService createUserService;

    /**
     * Constructor with dependency injection.
     *
     * @param createUserService core user create business logic
     */
    public CreateUserResponseCreatorService(final ICreateUserService createUserService) {
        Check.isNotNull(createUserService, "createUserService");

        this.createUserService = createUserService;
    }

    @Override
    public EntityCreatedResponse create(final CreateUserRequest request) {
        Check.isNotNull(request, "request");

        String entityId = createUserService.save(request);

        return new EntityCreatedResponse(entityId);
    }
}
