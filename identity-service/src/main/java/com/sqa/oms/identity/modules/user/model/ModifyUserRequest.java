package com.sqa.oms.identity.modules.user.model;

import com.sqa.oms.identity.annotation.Lowercase;
import com.sqa.oms.identity.annotation.Trimmed;
import com.sqa.oms.identity.annotation.UppercaseList;
import com.sqa.oms.identity.constants.Constants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @Min(message = "Status must be either 0 or 1", value = Constants.Status.INACTIVE_STATUS)
    @Max(message = "Status must be either 0 or 1", value = Constants.Status.ACTIVE_STATUS)
    @NotNull(message = "Status can not be null")
    private Short status;

    @NotNull(message = "Group ids can not be null")
    @UppercaseList
    private List<String> groupIds = new ArrayList<>();
}
