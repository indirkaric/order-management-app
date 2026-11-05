package com.sqa.oms.identity.dto;

import lombok.Data;

/**
 * Service layer representation of a permission group, returned by {@code IGroupService} to the
 * response creator services.
 */
@Data
public class GroupDTO {

    private String entityId;

    private String name;

    private String description;

    private Short status;
}
