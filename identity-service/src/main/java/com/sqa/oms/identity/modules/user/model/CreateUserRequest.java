package com.sqa.oms.identity.modules.user.model;

import com.sqa.oms.identity.annotation.Lowercase;
import com.sqa.oms.identity.annotation.Trimmed;
import com.sqa.oms.identity.annotation.UppercaseList;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Request body of the create user endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "Firstname can not be empty")
    @Trimmed
    private String firstName;

    @NotBlank(message = "Lastname can not be empty")
    @Trimmed
    private String lastName;

    @Email(message = "Email is not a valid email address")
    @NotBlank(message = "Email can not be empty")
    @Trimmed
    @Lowercase
    private String email;

    @NotNull(message = "Group ids can not be null")
    @UppercaseList
    private List<String> groupIds = new ArrayList<>();
}
