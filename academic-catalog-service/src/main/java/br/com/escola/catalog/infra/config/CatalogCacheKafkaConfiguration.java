package br.com.escola.catalog.infra.config;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import br.com.escola.catalog.application.event.IntegrationEventEnvelope;

@Configuration
@ConditionalOnProperty(
        name = {"catalog.cache.enabled", "catalog.cache.kafka.enabled"},
        havingValue = "true")
public class CatalogCacheKafkaConfiguration {

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, IntegrationEventEnvelope> catalogCacheKafkaListenerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        JsonDeserializer<IntegrationEventEnvelope> valueDeserializer =
                new JsonDeserializer<>(IntegrationEventEnvelope.class, false);
        valueDeserializer.addTrustedPackages("br.com.escola.catalog.application.event");
        var consumerFactory = new DefaultKafkaConsumerFactory<String, IntegrationEventEnvelope>(
                Map.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest",
                        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false),
                new StringDeserializer(), valueDeserializer);
        var factory = new ConcurrentKafkaListenerContainerFactory<String, IntegrationEventEnvelope>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
