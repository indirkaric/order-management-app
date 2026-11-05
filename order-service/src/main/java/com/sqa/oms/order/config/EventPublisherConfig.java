package com.sqa.oms.order.config;

import com.sqa.oms.common.events.IDomainEventPublisher;
import com.sqa.oms.common.events.KafkaDomainEventPublisher;
import com.sqa.oms.common.events.OmsEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class EventPublisherConfig {

    @Value("${oms.kafka.topic.audit-events}")
    private String auditEventsTopic;

    /**
     * Exposes the shared Kafka backed publisher from oms-common-events as a Spring bean.
     *
     * @param kafkaTemplate template configured by the Kafka auto configuration
     * @return IDomainEventPublisher
     */
    @Bean
    public IDomainEventPublisher domainEventPublisher(final KafkaTemplate<String, OmsEvent> kafkaTemplate) {
        return new KafkaDomainEventPublisher(kafkaTemplate, auditEventsTopic);
    }
}
