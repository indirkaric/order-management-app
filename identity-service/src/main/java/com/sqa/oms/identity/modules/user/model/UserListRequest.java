package com.sqa.oms.identity.modules.user.model;

import com.sqa.oms.identity.annotation.Lowercase;
import com.sqa.oms.identity.annotation.Trimmed;
import com.sqa.oms.identity.annotation.UppercaseList;
import com.sqa.oms.identity.enums.ExpandOption;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Filters of the user list endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserListRequest {

    @UppercaseList
    private List<String> ids;

    @Trimmed
    @Lowercase
    private String email;

    private Short status;

    @Trimmed
    private String textSearch;

    @UppercaseList
    private List<String> groupIds;

    private ExpandOption expand;
}
