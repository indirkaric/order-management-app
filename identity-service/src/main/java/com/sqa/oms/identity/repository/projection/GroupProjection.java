package com.sqa.oms.identity.repository.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Column level projection of the group table, selected directly by the Criteria API queries
 * instead of loading the full {@code Group} entity.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupProjection {

    private String entityId;

    private String name;

    private String description;

    private Short status;
}
