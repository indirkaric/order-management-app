package com.sqa.oms.identity.modules.group.model;

import com.sqa.oms.identity.annotation.Trimmed;
import com.sqa.oms.identity.annotation.UppercaseList;
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
    @Trimmed
    private String name;

    private String description;

    @NotEmpty(message = "Group must have at least one permission")
    @UppercaseList
    private List<String> permissionIds;
}
