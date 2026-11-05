package com.sqa.oms.identity.exception;

import com.sqa.oms.identity.exception.response.BusinessExceptionResponse;
import com.sqa.oms.identity.exception.response.RestApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;

/**
 * Exception controller. Designed for extension.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Translates a missing entity into a 404 response.
     *
     * @param exception exception to handle
     * @return ResponseEntity with exception
     */
    @ExceptionHandler(value = EntityNotFoundException.class)
    public ResponseEntity<RestApiResponse> exception(final EntityNotFoundException exception) {
        LOGGER.error(exception.getMessage(), exception);
        return new ResponseEntity<>(new RestApiResponse(exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    /**
     * Translates a business rule violation into a 422 response.
     *
     * @param exception exception to handle
     * @return ResponseEntity with exception
     */
    @ExceptionHandler(value = BusinessException.class)
    public ResponseEntity<BusinessExceptionResponse> exception(final BusinessException exception) {
        LOGGER.error(exception.getMessage(), exception);
        return new ResponseEntity<>(new BusinessExceptionResponse(exception.getMessages()),
                HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Translates an invalid argument into a 400 response.
     *
     * @param exception exception to handle
     * @return ResponseEntity with exception
     */
    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<BusinessExceptionResponse> exception(final IllegalArgumentException exception) {
        LOGGER.error(exception.getMessage(), exception);
        return new ResponseEntity<>(new BusinessExceptionResponse(
                Collections.singletonList(exception.getMessage())), HttpStatus.BAD_REQUEST);
    }

    /**
     * Translates any unhandled exception into a 500 response.
     *
     * @param exception exception to handle
     * @return ResponseEntity with exception
     * @throws AccessDeniedException   rethrown so Spring Security answers with 403
     * @throws AuthenticationException rethrown so Spring Security answers with 401
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<RestApiResponse> handleApiException(
            final Exception exception) throws AccessDeniedException, AuthenticationException {

        if (exception instanceof AuthenticationException) {
            throw ((AuthenticationException) exception);
        } else if (exception instanceof AccessDeniedException) {
            throw ((AccessDeniedException) exception);
        }

        LOGGER.error(exception.getMessage(), exception);
        return new ResponseEntity<>(new RestApiResponse("Something went wrong"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
