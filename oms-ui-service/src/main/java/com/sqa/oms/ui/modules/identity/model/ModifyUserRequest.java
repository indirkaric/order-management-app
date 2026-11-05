package com.sqa.oms.ui.modules.identity.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Request body of the modify user endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModifyUserRequest {

    @NotBlank(message = "Firstname can not be empty")
    private String firstName;

    @NotBlank(message = "Lastname can not be empty")
    private String lastName;

    @Email(message = "Email is not a valid email address")
    @NotBlank(message = "Email can not be empty")
    private String email;

    @NotNull(message = "Status can not be null")
    private Short status;

    @NotNull(message = "Group ids can not be null")
    private List<String> groupIds = new ArrayList<>();
}
