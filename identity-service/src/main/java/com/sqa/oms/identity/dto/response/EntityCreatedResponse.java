package com.sqa.oms.identity.dto.response;

/**
 * Response body returned after an entity has been created.
 *
 * @param id business key of the created entity
 */
public record EntityCreatedResponse(String id) {
}
