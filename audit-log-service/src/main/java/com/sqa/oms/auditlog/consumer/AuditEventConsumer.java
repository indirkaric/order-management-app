package com.sqa.oms.auditlog.consumer;

import com.sqa.oms.auditlog.mapper.IAuditLogMapper;
import com.sqa.oms.auditlog.service.store.IAuditLogStore;
import com.sqa.oms.auditlog.util.Check;
import com.sqa.oms.common.events.OmsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes every data change event emitted by the order management services.
 */
@Component
public final class AuditEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditEventConsumer.class);

    private final IAuditLogStore auditLogStore;

    private final IAuditLogMapper auditLogMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param auditLogStore  the active audit log store
     * @param auditLogMapper mapper from the event contract to the store model
     */
    public AuditEventConsumer(final IAuditLogStore auditLogStore, final IAuditLogMapper auditLogMapper) {
        Check.isNotNull(auditLogStore, "auditLogStore");
        Check.isNotNull(auditLogMapper, "auditLogMapper");
        this.auditLogStore = auditLogStore;
        this.auditLogMapper = auditLogMapper;
    }

    /**
     * Stores a single data change event.
     *
     * @param event the event emitted by a producing service
     */
    @KafkaListener(topics = "${oms.kafka.topic.audit-events}", groupId = "audit-log-service")
    public void consume(final OmsEvent event) {

        if (event == null || event.getEventId() == null) {
            LOGGER.warn("Discarding audit event without an event id");
            return;
        }

        // Idempotency guard: a redelivery after a consumer crash or a partition rebalance must not create a
        // second row for the same event, so the already stored event is acknowledged without being saved again.
        if (auditLogStore.existsByEventId(event.getEventId())) {
            LOGGER.debug("Audit event {} is already stored, skipping", event.getEventId());
            return;
        }

        auditLogStore.save(auditLogMapper.toEntry(event));
    }
}
