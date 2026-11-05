package com.sqa.oms.identity.event;

import com.sqa.oms.common.events.IDomainEventPublisher;
import com.sqa.oms.common.events.OmsEvent;
import com.sqa.oms.identity.constants.Constants;
import com.sqa.oms.identity.util.Check;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Bridges the local application events to Kafka.
 */
@Component
public class DomainChangeEventListener {

    private final IDomainEventPublisher domainEventPublisher;

    /**
     * Constructor with dependency injection.
     *
     * @param domainEventPublisher publisher writing to the audit topic
     */
    public DomainChangeEventListener(final IDomainEventPublisher domainEventPublisher) {
        Check.isNotNull(domainEventPublisher, "domainEventPublisher");
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * Publishes the event to Kafka. AFTER_COMMIT keeps the audit trail consistent with the database:
     * a rolled back transaction must never emit an event, and the broker being unreachable must never
     * roll back a committed change.
     *
     * @param event the committed domain change
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public final void onDomainChange(final DomainChangeEvent event) {
        domainEventPublisher.publish(OmsEvent.of(
                event.entityType(),
                event.action(),
                event.entityId(),
                Constants.SOURCE_SERVICE,
                event.performedBy(),
                event.payload()));
    }
}
