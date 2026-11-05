package com.sqa.oms.identity.modules.group.model;

import com.sqa.oms.identity.annotation.Trimmed;
import com.sqa.oms.identity.annotation.UppercaseList;
import com.sqa.oms.identity.constants.Constants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request body of the update group endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateGroupRequest {

    @NotBlank(message = "Group name cannot be empty")
    @Trimmed
    private String name;

    private String description;

    @Min(message = "Status must be either 0 or 1", value = Constants.Status.INACTIVE_STATUS)
    @Max(message = "Status must be either 0 or 1", value = Constants.Status.ACTIVE_STATUS)
    @NotNull(message = "Status of group cannot be null")
    private Short status;

    @NotEmpty(message = "Group must have at least one permission")
    @UppercaseList
    private List<String> permissionIds;
}
