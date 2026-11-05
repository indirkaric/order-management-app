package com.sqa.oms.identity.dto;

import lombok.Data;

/**
 * Service layer representation of a user, returned by {@code IUserService} to the response
 * creator services.
 */
@Data
public class UserDTO {

    private String entityId;

    private String firstName;

    private String lastName;

    private String email;

    private Short status;
}
