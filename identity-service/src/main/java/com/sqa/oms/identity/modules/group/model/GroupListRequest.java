package com.sqa.oms.identity.modules.group.model;

import com.sqa.oms.identity.annotation.Trimmed;
import com.sqa.oms.identity.annotation.UppercaseList;
import com.sqa.oms.identity.enums.ExpandOption;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Filters of the group list endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupListRequest {

    @UppercaseList
    private List<String> ids;

    private Short status;

    @Trimmed
    private String textSearch;

    private ExpandOption expand;
}
