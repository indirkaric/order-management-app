package com.sqa.oms.ui.modules.identity.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Filters of the permission list endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionListRequest {

    private List<String> ids;

    private Short status;

    private String textSearch;
}
