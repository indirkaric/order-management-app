package com.sqa.oms.ui.modules.identity.model;

/**
 * Response body returned after a user or group has been created, mirroring identity-service's
 * own {@code EntityCreatedResponse} shape.
 *
 * @param id business key of the created entity
 */
public record EntityCreatedResponse(String id) {
}
