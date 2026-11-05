package com.sqa.oms.order.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqa.oms.common.events.EntityType;
import com.sqa.oms.common.events.EventAction;
import com.sqa.oms.common.events.IDomainEventPublisher;
import com.sqa.oms.common.events.OmsEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DomainEventListenerTest {

    @Mock
    private IDomainEventPublisher domainEventPublisher;

    private DomainEventListener domainEventListener;

    @BeforeEach
    void setUp() {
        domainEventListener = new DomainEventListener(domainEventPublisher, new ObjectMapper());
    }

    @Test
    void shouldPublishTheOmsEventBuiltFromTheLocalChangeEvent() {
        DomainChangeEvent event = new DomainChangeEvent(
                EntityType.ORDER, EventAction.CREATE, "ORD-00000001", "caller-id", "payload");

        domainEventListener.onDomainChange(event);

        ArgumentCaptor<OmsEvent> captor = ArgumentCaptor.forClass(OmsEvent.class);
        verify(domainEventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getEntityType()).isEqualTo(EntityType.ORDER);
        assertThat(captor.getValue().getAction()).isEqualTo(EventAction.CREATE);
        assertThat(captor.getValue().getEntityId()).isEqualTo("ORD-00000001");
    }

    @Test
    void shouldSwallowPublishFailuresInsteadOfPropagatingThem() {
        DomainChangeEvent event = new DomainChangeEvent(
                EntityType.ORDER, EventAction.DELETE, "ORD-00000001", "caller-id", null);
        doThrow(new RuntimeException("kafka unavailable"))
                .when(domainEventPublisher).publish(org.mockito.ArgumentMatchers.any());

        domainEventListener.onDomainChange(event);

        verify(domainEventPublisher).publish(org.mockito.ArgumentMatchers.any());
    }
}
