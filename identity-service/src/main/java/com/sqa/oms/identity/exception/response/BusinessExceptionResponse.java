package com.sqa.oms.identity.exception.response;

import java.util.List;

/**
 * Response body of a business rule violation.
 *
 * @param messages the violation messages
 */
public record BusinessExceptionResponse(List<String> messages) {
}
