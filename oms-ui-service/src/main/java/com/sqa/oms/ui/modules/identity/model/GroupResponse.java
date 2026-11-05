package com.sqa.oms.ui.modules.identity.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

/**
 * DTO of the Group entity, mirroring identity-service's own response shape.
 */
@JsonPropertyOrder({"id", "name", "description", "status", "permissions"})
@Data
public class GroupResponse {

    @JsonProperty("id")
    private String entityId;

    private String name;

    private String description;

    private Short status;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> permissions;
}
