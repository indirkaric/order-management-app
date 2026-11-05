package com.sqa.oms.order.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqa.oms.common.events.IDomainEventPublisher;
import com.sqa.oms.common.events.OmsEvent;
import com.sqa.oms.order.constants.Constants;
import com.sqa.oms.order.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public final class DomainEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainEventListener.class);

    private final IDomainEventPublisher domainEventPublisher;

    private final ObjectMapper objectMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param domainEventPublisher kafka backed publisher
     * @param objectMapper         mapper used to serialize the event payload
     */
    public DomainEventListener(final IDomainEventPublisher domainEventPublisher, final ObjectMapper objectMapper) {
        Check.isNotNull(domainEventPublisher, "domainEventPublisher");
        Check.isNotNull(objectMapper, "objectMapper");
        this.domainEventPublisher = domainEventPublisher;
        this.objectMapper = objectMapper;
    }

    /**
     * Sends the event to Kafka only after the originating transaction committed, so that no
     * consumer can ever observe a change that was rolled back.
     *
     * @param event local domain change event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDomainChange(final DomainChangeEvent event) {
        try {
            JsonNode payload = event.payload() == null ? null : objectMapper.valueToTree(event.payload());

            domainEventPublisher.publish(OmsEvent.of(
                    event.entityType(),
                    event.action(),
                    event.entityId(),
                    Constants.SOURCE_SERVICE,
                    event.performedBy(),
                    payload));
        } catch (RuntimeException exception) {
            LOGGER.error("Unable to publish the {} event for entity {}",
                    event.action(), event.entityId(), exception);
        }
    }
}
