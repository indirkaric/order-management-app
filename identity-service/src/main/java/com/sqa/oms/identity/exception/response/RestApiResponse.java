package com.sqa.oms.identity.exception.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Generic single message error response.
 */
@Getter
@AllArgsConstructor
public final class RestApiResponse {

    private final String message;

    /**
     * Serializes this response to its JSON representation.
     *
     * @return json value as String
     * @throws JsonProcessingException when the response cannot be serialized
     */
    public String toJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }
}
