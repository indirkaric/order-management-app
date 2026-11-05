package com.sqa.oms.ui.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqa.oms.ui.client.response.ClientFileResponseWrapper;
import com.sqa.oms.ui.client.response.ClientResponseWrapper;
import com.sqa.oms.ui.exception.BusinessException;
import com.sqa.oms.ui.exception.DownstreamClientException;
import com.sqa.oms.ui.exception.EntityNotFoundException;
import com.sqa.oms.ui.exception.ServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResponseUtilTest {

    private static final String SERVICE_NAME = "Order";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldMapTheBodyOnSuccess() throws Exception {
        JsonNode body = objectMapper.createObjectNode().put("id", "1");
        ClientResponseWrapper wrapper = new ClientResponseWrapper(HttpStatus.OK, body);

        JsonNode result = ResponseUtil.handleResponse(wrapper, JsonNode.class, SERVICE_NAME);

        assertThat(result.get("id").asText()).isEqualTo("1");
    }

    @Test
    void shouldReturnNullOnNoContent() throws Exception {
        ClientResponseWrapper wrapper = new ClientResponseWrapper(HttpStatus.NO_CONTENT, null);

        assertThat(ResponseUtil.handleResponse(wrapper, JsonNode.class, SERVICE_NAME)).isNull();
    }

    @Test
    void shouldThrowEntityNotFoundOn404() {
        ClientResponseWrapper wrapper = new ClientResponseWrapper(HttpStatus.NOT_FOUND, null);

        assertThatThrownBy(() -> ResponseUtil.handleResponse(wrapper, JsonNode.class, SERVICE_NAME))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowBusinessExceptionOn422WithMessages() {
        com.fasterxml.jackson.databind.node.ObjectNode body = objectMapper.createObjectNode();
        body.putArray("messages").add("quantity must be positive");
        ClientResponseWrapper wrapper = new ClientResponseWrapper(HttpStatus.UNPROCESSABLE_ENTITY, body);

        assertThatThrownBy(() -> ResponseUtil.handleResponse(wrapper, JsonNode.class, SERVICE_NAME))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getMessages())
                        .containsExactly("quantity must be positive"));
    }

    @Test
    void shouldThrowDownstreamClientExceptionOnOther4xx() {
        ClientResponseWrapper wrapper = new ClientResponseWrapper(HttpStatus.BAD_REQUEST, null);

        assertThatThrownBy(() -> ResponseUtil.handleResponse(wrapper, JsonNode.class, SERVICE_NAME))
                .isInstanceOf(DownstreamClientException.class);
    }

    @Test
    void shouldThrowServiceUnavailableWhenTheWrapperIsNull() {
        assertThatThrownBy(() -> ResponseUtil.handleResponse(null, JsonNode.class, SERVICE_NAME))
                .isInstanceOf(ServiceUnavailableException.class);
    }

    @Test
    void shouldThrowOnServerError() {
        ClientResponseWrapper wrapper = new ClientResponseWrapper(HttpStatus.INTERNAL_SERVER_ERROR, null);

        assertThatThrownBy(() -> ResponseUtil.handleResponse(wrapper, JsonNode.class, SERVICE_NAME))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldReturnTheWrapperOnSuccessWhenHandlingFileResponse() throws Exception {
        ClientFileResponseWrapper wrapper = new ClientFileResponseWrapper(
                HttpStatus.OK, "bytes".getBytes(), "application/pdf", null);

        assertThat(ResponseUtil.handleFileResponse(wrapper, SERVICE_NAME)).isEqualTo(wrapper);
    }

    @Test
    void shouldThrowNotFoundOn404WhenHandlingFileResponse() {
        ClientFileResponseWrapper wrapper = new ClientFileResponseWrapper(HttpStatus.NOT_FOUND, null, null, null);

        assertThatThrownBy(() -> ResponseUtil.handleFileResponse(wrapper, SERVICE_NAME))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
