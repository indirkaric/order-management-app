package com.sqa.oms.identity.repository.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Column level projection of the user table, selected directly by the Criteria API queries
 * instead of loading the full {@code User} entity.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProjection {

    private String entityId;

    private String firstName;

    private String lastName;

    private String email;

    private Short status;
}
