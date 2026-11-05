package com.sqa.oms.ui.modules.identity.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request body of the create group endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateGroupRequest {

    @NotBlank(message = "Group name cannot be empty")
    private String name;

    private String description;

    @NotEmpty(message = "Group must have at least one permission")
    private List<String> permissionIds;
}
