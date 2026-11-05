package com.sqa.oms.common.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaDomainEventPublisher implements IDomainEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);

    private final KafkaTemplate<String, OmsEvent> kafkaTemplate;
    private final String topic;

    public KafkaDomainEventPublisher(final KafkaTemplate<String, OmsEvent> kafkaTemplate, final String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(final OmsEvent event) {
        // key = entityId so all events for the same entity land on the same partition and keep order
        kafkaTemplate.send(topic, event.getEntityId(), event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        LOGGER.error("Failed to publish event {} for entity {}", event.getEventType(), event.getEntityId(), exception);
                    }
                });
    }
}
