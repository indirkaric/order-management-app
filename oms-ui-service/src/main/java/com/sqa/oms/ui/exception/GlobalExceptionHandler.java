package com.sqa.oms.ui.exception;

import com.sqa.oms.ui.exception.response.BusinessExceptionResponse;
import com.sqa.oms.ui.exception.response.RestApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.List;

/**
 * Exception controller. Designed for extension.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String GENERIC_ERROR_MESSAGE = "Something went wrong";

    /**
     * Handles a missing entity reported by a downstream service.
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
     * Handles business rule violations reported by a downstream service.
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
     * Handles invalid arguments.
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
     * Handles an unreachable downstream service.
     *
     * @param exception exception to handle
     * @return ResponseEntity with exception
     */
    @ExceptionHandler(value = ServiceUnavailableException.class)
    public ResponseEntity<RestApiResponse> exception(final ServiceUnavailableException exception) {
        LOGGER.error(exception.getMessage(), exception);
        return new ResponseEntity<>(new RestApiResponse(exception.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Forwards a downstream client error to the caller with the original status and body,
     * so the frontend sees the real reason instead of a generic gateway error.
     *
     * @param exception exception to handle
     * @return ResponseEntity with the downstream status and body
     */
    @ExceptionHandler(value = DownstreamClientException.class)
    public ResponseEntity<Object> exception(final DownstreamClientException exception) {
        LOGGER.error("Downstream service returned {}", exception.getStatusCode());
        Object body = exception.getResponseBody() == null
                ? new RestApiResponse(exception.getStatusCode().getReasonPhrase())
                : exception.getResponseBody();
        return new ResponseEntity<>(body, exception.getStatusCode());
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
     * Handles every other exception. Designed for extension.
     *
     * @param exception exception to handle
     * @return ResponseEntity with exception
     * @throws AccessDeniedException   when the exception must be handled by Spring Security
     * @throws AuthenticationException when the exception must be handled by Spring Security
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<RestApiResponse> handleApiException(final Exception exception)
            throws AccessDeniedException, AuthenticationException {

        if (exception instanceof AuthenticationException authenticationException) {
            throw authenticationException;
        } else if (exception instanceof AccessDeniedException accessDeniedException) {
            throw accessDeniedException;
        }

        LOGGER.error(exception.getMessage(), exception);
        return new ResponseEntity<>(new RestApiResponse(GENERIC_ERROR_MESSAGE), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static String describeFieldError(final FieldError fieldError) {
        return fieldError.getField() + " " + fieldError.getDefaultMessage();
    }
}
