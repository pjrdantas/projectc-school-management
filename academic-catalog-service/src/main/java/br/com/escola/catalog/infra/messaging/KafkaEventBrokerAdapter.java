package br.com.escola.catalog.infra.messaging;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import br.com.escola.catalog.application.event.BrokerPublication;
import br.com.escola.catalog.application.event.IntegrationEventEnvelope;
import br.com.escola.catalog.application.port.out.EventBrokerPort;

@Component
public class KafkaEventBrokerAdapter implements EventBrokerPort {

    private final KafkaTemplate<String, IntegrationEventEnvelope> kafkaTemplate;
    private final Duration sendTimeout;

    public KafkaEventBrokerAdapter(
            KafkaTemplate<String, IntegrationEventEnvelope> kafkaTemplate,
            @Value("${catalog.outbox.publisher.send-timeout:PT10S}") Duration sendTimeout) {
        this.kafkaTemplate = kafkaTemplate;
        this.sendTimeout = sendTimeout;
    }

    @Override
    public BrokerPublication publicar(String topic, String key, IntegrationEventEnvelope event) {
        try {
            var result = kafkaTemplate.send(topic, key, event)
                    .get(sendTimeout.toMillis(), TimeUnit.MILLISECONDS);
            var metadata = result.getRecordMetadata();
            return new BrokerPublication(metadata.topic(), metadata.partition(), metadata.offset());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Publicacao Kafka interrompida", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao publicar evento no Kafka", exception);
        }
    }
}
