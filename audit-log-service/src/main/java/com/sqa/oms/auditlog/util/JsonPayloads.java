package com.sqa.oms.auditlog.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Converts audit event payloads between their tree representation and their stored string form.
 */
public final class JsonPayloads {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonPayloads() {
    }

    /**
     * Serializes a payload node to its string representation.
     *
     * @param payload payload node, may be null
     * @return the serialized payload or null when there is no payload
     */
    public static String toJson(final JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return null;
        }

        return payload.toString();
    }

    /**
     * Parses a stored payload string back into a payload node.
     *
     * @param payload serialized payload, may be null
     * @return the parsed node or null when there is no payload
     */
    public static JsonNode toNode(final String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readTree(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Stored audit payload is not valid JSON.", exception);
        }
    }
}
