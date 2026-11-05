package com.sqa.oms.identity.config;

import com.sqa.oms.common.events.IDomainEventPublisher;
import com.sqa.oms.common.events.KafkaDomainEventPublisher;
import com.sqa.oms.common.events.OmsEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Wires the shared Kafka event publisher of the order management system.
 */
@Configuration
public class EventPublisherConfig {

    @Value("${oms.kafka.topic.audit-events}")
    private String auditEventsTopic;

    /**
     * Creates the publisher that writes domain events to the audit topic.
     *
     * @param kafkaTemplate template used to send the events
     * @return IDomainEventPublisher
     */
    @Bean
    public IDomainEventPublisher domainEventPublisher(final KafkaTemplate<String, OmsEvent> kafkaTemplate) {
        return new KafkaDomainEventPublisher(kafkaTemplate, auditEventsTopic);
    }
}
