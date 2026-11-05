package com.sqa.oms.auditlog.exception.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Generic single message response payload.
 */
@Getter
@AllArgsConstructor
public final class RestApiResponse {

    private final String message;

    /**
     * Turn the value of json to String.
     *
     * @return json value as String
     * @throws JsonProcessingException when the payload cannot be serialized
     */
    public String toJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }
}
