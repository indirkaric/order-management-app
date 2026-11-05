package com.sqa.oms.identity.modules.user.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sqa.oms.identity.modules.group.model.GroupSummaryResponse;
import lombok.Data;

import java.util.List;

/**
 * DTO of the User entity.
 */
@JsonPropertyOrder({"id", "firstName", "lastName", "email", "status", "permissionGroups"})
@Data
public class UserResponse {

    @JsonProperty("id")
    private String entityId;

    private String firstName;

    private String lastName;

    private String email;

    private Short status;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<GroupSummaryResponse> permissionGroups;
}
