package com.sqa.oms.identity.repository.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Short, column level projection of a group a user belongs to, used to build the
 * {@code permissionGroups} expansion of the user response without loading full {@code Group}
 * entities.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupSummaryProjection {

    private String entityId;

    private String name;
}
