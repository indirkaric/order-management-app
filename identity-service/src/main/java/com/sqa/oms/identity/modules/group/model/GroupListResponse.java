package com.sqa.oms.identity.modules.group.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Paged response of the group list endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupListResponse {

    private List<GroupResponse> data = new ArrayList<>();

    private long count;
}
