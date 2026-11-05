package com.sqa.oms.ui.modules.identity.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

/**
 * Short form of a permission group, embedded in the user response.
 */
@JsonPropertyOrder({"id", "name"})
@Data
public class GroupSummaryResponse {

    @JsonProperty("id")
    private String entityId;

    private String name;
}
