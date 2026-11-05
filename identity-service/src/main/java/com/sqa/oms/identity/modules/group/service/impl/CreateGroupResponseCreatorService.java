package com.sqa.oms.identity.modules.group.service.impl;

import com.sqa.oms.identity.dto.response.EntityCreatedResponse;
import com.sqa.oms.identity.modules.group.model.CreateGroupRequest;
import com.sqa.oms.identity.modules.group.service.ICreateGroupResponseCreatorService;
import com.sqa.oms.identity.service.group.ICreateGroupService;
import com.sqa.oms.identity.util.Check;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the group create response builder.
 */
@Service
public class CreateGroupResponseCreatorService implements ICreateGroupResponseCreatorService {

    private final ICreateGroupService createGroupService;

    /**
     * Constructor with dependency injection.
     *
     * @param createGroupService core group create business logic
     */
    public CreateGroupResponseCreatorService(final ICreateGroupService createGroupService) {
        Check.isNotNull(createGroupService, "createGroupService");

        this.createGroupService = createGroupService;
    }

    @Override
    public EntityCreatedResponse create(final CreateGroupRequest request) {
        Check.isNotNull(request, "request");

        String entityId = createGroupService.save(request);

        return new EntityCreatedResponse(entityId);
    }
}
