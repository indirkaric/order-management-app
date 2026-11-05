package com.sqa.oms.ui.modules.identity.model;

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

    private List<String> ids;

    private String email;

    private Short status;

    private String textSearch;

    private List<String> groupIds;

    private ExpandOption expand;
}
