package com.sqa.oms.order.exception.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
