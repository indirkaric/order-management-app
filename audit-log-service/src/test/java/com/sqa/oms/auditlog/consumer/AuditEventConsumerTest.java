package com.sqa.oms.auditlog.consumer;

import com.sqa.oms.auditlog.mapper.IAuditLogMapper;
import com.sqa.oms.auditlog.service.store.IAuditLogStore;
import com.sqa.oms.auditlog.service.store.model.AuditLogEntry;
import com.sqa.oms.common.events.EntityType;
import com.sqa.oms.common.events.EventAction;
import com.sqa.oms.common.events.OmsEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditEventConsumerTest {

    @Mock
    private IAuditLogStore auditLogStore;

    @Mock
    private IAuditLogMapper auditLogMapper;

    private AuditEventConsumer auditEventConsumer;

    @BeforeEach
    void setUp() {
        auditEventConsumer = new AuditEventConsumer(auditLogStore, auditLogMapper);
    }

    @Test
    void shouldDiscardEventsWithoutAnEventId() {
        OmsEvent event = OmsEvent.of(EntityType.ORDER, EventAction.CREATE, "ORD-1", "order-service", "caller", null);
        event.setEventId(null);

        auditEventConsumer.consume(event);

        verify(auditLogStore, never()).save(any());
    }

    @Test
    void shouldSkipAlreadyStoredEvents() {
        OmsEvent event = OmsEvent.of(EntityType.ORDER, EventAction.CREATE, "ORD-1", "order-service", "caller", null);
        when(auditLogStore.existsByEventId(event.getEventId())).thenReturn(true);

        auditEventConsumer.consume(event);

        verify(auditLogStore, never()).save(any());
    }

    @Test
    void shouldStoreNewEvents() {
        OmsEvent event = OmsEvent.of(EntityType.ORDER, EventAction.CREATE, "ORD-1", "order-service", "caller", null);
        AuditLogEntry entry = AuditLogEntry.builder().eventId(event.getEventId()).build();

        when(auditLogStore.existsByEventId(event.getEventId())).thenReturn(false);
        when(auditLogMapper.toEntry(event)).thenReturn(entry);

        auditEventConsumer.consume(event);

        verify(auditLogStore).save(entry);
    }
}
