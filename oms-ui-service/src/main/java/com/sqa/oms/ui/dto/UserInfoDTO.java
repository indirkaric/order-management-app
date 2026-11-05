package com.sqa.oms.ui.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Caller identity forwarded to the downstream services in the Sqa-User-Id header.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {

    private String userId;

    private List<String> authorities;
}
