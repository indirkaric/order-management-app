package com.sqa.oms.ui.exception.response;

import java.util.List;

/**
 * Payload returned for business rule violations.
 *
 * @param messages list of violation messages
 */
public record BusinessExceptionResponse(List<String> messages) {
}
