package com.sqa.oms.auditlog.exception.response;

import java.util.List;

/**
 * Response payload returned for business rule violations.
 *
 * @param messages the validation messages
 */
public record BusinessExceptionResponse(List<String> messages) {
}
