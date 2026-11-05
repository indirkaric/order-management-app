package com.sqa.oms.storage.exception;

import com.sqa.oms.storage.exception.response.BusinessExceptionResponse;
import com.sqa.oms.storage.exception.response.RestApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    public void shouldReturnBadRequestForMethodArgumentNotValidException() {

        FieldError fieldError = new FieldError("request", "filePath", "must not be blank");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<BusinessExceptionResponse> response = globalExceptionHandler.exception(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(List.of("filePath must not be blank"), response.getBody().messages());
    }

    @Test
    public void shouldReturnUnprocessableEntityForBusinessException() {

        ResponseEntity<BusinessExceptionResponse> response =
                globalExceptionHandler.exception(new BusinessException("invalid"));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(List.of("invalid"), response.getBody().messages());
    }

    @Test
    public void shouldReturnBadRequestForIllegalArgumentException() {

        ResponseEntity<BusinessExceptionResponse> response =
                globalExceptionHandler.exception(new IllegalArgumentException("bad"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void shouldReturnInternalServerErrorForUnexpectedException() {

        ResponseEntity<RestApiResponse> response =
                globalExceptionHandler.handleApiException(new IllegalStateException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void shouldRethrowSecurityExceptions() {

        assertThrows(AccessDeniedException.class,
                () -> globalExceptionHandler.handleApiException(new AccessDeniedException("denied")));
        assertThrows(BadCredentialsException.class,
                () -> globalExceptionHandler.handleApiException(new BadCredentialsException("nope")));
    }

    @Test
    public void shouldBuildBusinessExceptionFromMessageList() {

        BusinessException exception = new BusinessException(List.of("a", "b"));

        assertEquals(List.of("a", "b"), exception.getMessages());
    }
}
