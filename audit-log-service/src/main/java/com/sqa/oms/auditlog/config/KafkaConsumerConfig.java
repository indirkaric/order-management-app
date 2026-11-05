package com.sqa.oms.auditlog.config;

import com.sqa.oms.common.events.OmsEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Kafka consumer infrastructure: retry policy, dead letter publishing and the listener container factory.
 */
@Configuration
public class KafkaConsumerConfig {

    private static final String DLT_SUFFIX = ".DLT";

    private static final int MAX_RETRY_ATTEMPTS = 3;

    private static final long INITIAL_BACK_OFF_INTERVAL_MS = 1000L;

    private static final double BACK_OFF_MULTIPLIER = 2.0;

    private final String bootstrapServers;

    /**
     * Constructor with dependency injection.
     *
     * @param bootstrapServers the Kafka bootstrap servers used by the dead letter producer
     */
    public KafkaConsumerConfig(@Value("${spring.kafka.bootstrap-servers}") final String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    /**
     * Template used by the dead letter recoverer.
     *
     * @return KafkaTemplate able to publish both raw bytes and deserialized events
     */
    @Bean
    public KafkaTemplate<Object, Object> deadLetterKafkaTemplate() {
        Map<String, Object> producerProperties = new HashMap<>();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // A record that failed deserialization carries raw bytes, one that failed in the listener carries an
        // OmsEvent, so the dead letter producer has to be able to serialize both shapes.
        Map<Class<?>, Serializer<?>> delegates = new LinkedHashMap<>();
        delegates.put(byte[].class, new ByteArraySerializer());
        delegates.put(Object.class, new JsonSerializer<>());

        ProducerFactory<Object, Object> producerFactory = new DefaultKafkaProducerFactory<>(
                producerProperties,
                new DelegatingByTypeSerializer(delegates),
                new DelegatingByTypeSerializer(delegates));

        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * Bounded exponential retry, after which the record is routed to the dead letter topic.
     *
     * @param deadLetterKafkaTemplate template used to publish to the dead letter topic
     * @return the common error handler of the listener containers
     */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(final KafkaTemplate<Object, Object> deadLetterKafkaTemplate) {
        // Permanently failing records are republished to "<original-topic>.DLT" on the same partition index
        // instead of being retried forever, which would block the partition for every later record.
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(deadLetterKafkaTemplate,
                (consumerRecord, exception) ->
                        new TopicPartition(consumerRecord.topic() + DLT_SUFFIX, consumerRecord.partition()));

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(MAX_RETRY_ATTEMPTS);
        backOff.setInitialInterval(INITIAL_BACK_OFF_INTERVAL_MS);
        backOff.setMultiplier(BACK_OFF_MULTIPLIER);

        return new DefaultErrorHandler(recoverer, backOff);
    }

    /**
     * Listener container factory with the dead letter error handler registered as common error handler.
     *
     * @param consumerFactory   the auto configured consumer factory
     * @param kafkaErrorHandler the error handler to register on every listener container
     * @return the listener container factory used by every KafkaListener of this service
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OmsEvent> kafkaListenerContainerFactory(
            final ConsumerFactory<String, OmsEvent> consumerFactory,
            final DefaultErrorHandler kafkaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, OmsEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        return factory;
    }
}
