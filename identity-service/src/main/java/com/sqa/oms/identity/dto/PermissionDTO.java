package com.sqa.oms.identity.dto;

import lombok.Data;

/**
 * Service layer representation of a permission, returned by {@code IPermissionService} to the
 * response creator services.
 */
@Data
public class PermissionDTO {

    private String entityId;

    private String name;

    private String description;

    private Short status;
}
