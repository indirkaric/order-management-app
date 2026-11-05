package com.sqa.oms.identity.modules.permission.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

/**
 * DTO of the Permission entity.
 */
@JsonPropertyOrder({"id", "name", "description", "status"})
@Data
public class PermissionResponse {

    @JsonProperty("id")
    private String entityId;

    private String name;

    private String description;

    private Short status;
}
