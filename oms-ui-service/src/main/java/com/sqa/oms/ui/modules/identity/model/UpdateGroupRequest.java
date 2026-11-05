package com.sqa.oms.ui.modules.identity.model;

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
    private String name;

    private String description;

    @NotNull(message = "Status of group cannot be null")
    private Short status;

    @NotEmpty(message = "Group must have at least one permission")
    private List<String> permissionIds;
}
