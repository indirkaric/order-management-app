package com.sqa.oms.order.exception;

import com.sqa.oms.order.exception.response.BusinessExceptionResponse;
import com.sqa.oms.order.exception.response.RestApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.List;

/**
 * Exception controller. Designed for extension.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Exception handling method. Designed for extension.
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
     * Exception handling method. Designed for extension.
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
     * Exception handling method. Designed for extension.
     *
     * @param exception exception to handle
     * @return ResponseEntity with exception
     */
    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<BusinessExceptionResponse> exception(final IllegalArgumentException exception) {
        LOGGER.error(exception.getMessage(), exception);
        return new ResponseEntity<>(new BusinessExceptionResponse(Collections
                .singletonList(exception.getMessage())),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Translates bean validation failures into the same error envelope as the other handlers.
     *
     * @param exception exception to handle
     * @return ResponseEntity with exception
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<BusinessExceptionResponse> exception(final MethodArgumentNotValidException exception) {
        List<String> messages = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(GlobalExceptionHandler::describeFieldError)
                .toList();

        LOGGER.error("Request validation failed: {}", messages);

        return new ResponseEntity<>(new BusinessExceptionResponse(messages), HttpStatus.BAD_REQUEST);
    }

    /**
     * Exception handling method. Designed for extension.
     *
     * @param exception exception to handle
     * @return ResponseEntity with exception
     * @throws AccessDeniedException   rethrown so that Spring Security can answer the caller
     * @throws AuthenticationException rethrown so that Spring Security can answer the caller
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<RestApiResponse> handleApiException(final Exception exception)
            throws AccessDeniedException, AuthenticationException {

        if (exception instanceof AuthenticationException) {
            throw ((AuthenticationException) exception);
        } else if (exception instanceof AccessDeniedException) {
            throw ((AccessDeniedException) exception);
        }

        LOGGER.error(exception.getMessage(), exception);

        return new ResponseEntity<>(new RestApiResponse("Something went wrong"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static String describeFieldError(final FieldError fieldError) {
        return fieldError.getField() + " " + fieldError.getDefaultMessage();
    }
}
