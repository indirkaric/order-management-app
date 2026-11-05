package com.sqa.oms.ui.modules.identity.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Paged response of the permission list endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionListResponse {

    private List<PermissionResponse> data = new ArrayList<>();

    private long count;
}
