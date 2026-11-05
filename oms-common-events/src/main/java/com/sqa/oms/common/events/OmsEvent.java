package com.sqa.oms.common.events;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OmsEvent {

    private String eventId;
    private String eventType;
    private EventAction action;
    private EntityType entityType;
    private String entityId;
    private String sourceService;
    private String performedBy;
    private Instant timestamp;
    private JsonNode payload;

    public static OmsEvent of(final EntityType entityType, final EventAction action, final String entityId,
                               final String sourceService, final String performedBy, final JsonNode payload) {
        return OmsEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(entityType.name() + "_" + action.pastTense())
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .sourceService(sourceService)
                .performedBy(performedBy)
                .timestamp(Instant.now())
                .payload(payload)
                .build();
    }
}
