package com.sqa.oms.identity.modules.group.service;

import com.sqa.oms.identity.dto.response.EntityCreatedResponse;
import com.sqa.oms.identity.modules.group.model.CreateGroupRequest;

/**
 * Builds the response of the group create endpoint.
 */
public interface ICreateGroupResponseCreatorService {

    /**
     * Creates a group and wraps its business key in a response.
     *
     * @param request the create request
     * @return EntityCreatedResponse
     */
    EntityCreatedResponse create(CreateGroupRequest request);
}
