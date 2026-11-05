package com.sqa.oms.identity.modules.permission.model;

import com.sqa.oms.identity.annotation.Trimmed;
import com.sqa.oms.identity.annotation.UppercaseList;
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

    @UppercaseList
    private List<String> ids;

    private Short status;

    @Trimmed
    private String textSearch;
}
