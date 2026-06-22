package br.com.escola.catalog.infra.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "catalog.outbox.publisher.enabled", havingValue = "true")
public class KafkaPublisherConfiguration {

    @Bean
    NewTopic catalogEventsTopic(
            @Value("${catalog.outbox.publisher.topic:school.catalog.events.v1}") String topic,
            @Value("${catalog.outbox.publisher.partitions:3}") int partitions) {
        return TopicBuilder.name(topic).partitions(partitions).replicas(1).build();
    }

    @Bean
    NewTopic catalogEventsDltTopic(
            @Value("${catalog.outbox.publisher.dlt-topic:school.catalog.events.v1.DLT}") String topic,
            @Value("${catalog.outbox.publisher.partitions:3}") int partitions) {
        return TopicBuilder.name(topic).partitions(partitions).replicas(1).build();
    }
}
