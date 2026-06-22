package br.com.escola.catalog.infra.messaging;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.escola.catalog.application.service.OutboxPublisherResult;
import br.com.escola.catalog.application.service.OutboxPublisherService;
import br.com.escola.catalog.application.service.OutboxPublisherSettings;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Component
@ConditionalOnProperty(name = "catalog.outbox.publisher.enabled", havingValue = "true")
public class OutboxPublisherScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxPublisherScheduler.class);

    private final OutboxPublisherService publisherService;
    private final OutboxPublisherSettings settings;
    private final Counter claimedCounter;
    private final Counter publishedCounter;
    private final Counter retryCounter;
    private final Counter dltCounter;
    private final Timer cycleTimer;

    public OutboxPublisherScheduler(
            OutboxPublisherService publisherService,
            MeterRegistry meterRegistry,
            @Value("${catalog.outbox.publisher.batch-size:50}") int batchSize,
            @Value("${catalog.outbox.publisher.max-attempts:5}") int maxAttempts,
            @Value("${catalog.outbox.publisher.lock-timeout:PT1M}") Duration lockTimeout,
            @Value("${catalog.outbox.publisher.initial-backoff:PT5S}") Duration initialBackoff,
            @Value("${catalog.outbox.publisher.max-backoff:PT5M}") Duration maxBackoff,
            @Value("${catalog.outbox.publisher.topic:school.catalog.events.v1}") String topic,
            @Value("${catalog.outbox.publisher.dlt-topic:school.catalog.events.v1.DLT}") String dltTopic) {
        this.publisherService = publisherService;
        this.settings = new OutboxPublisherSettings(
                batchSize, maxAttempts, lockTimeout, initialBackoff, maxBackoff, topic, dltTopic);
        this.claimedCounter = meterRegistry.counter("catalog.outbox.claimed");
        this.publishedCounter = meterRegistry.counter("catalog.outbox.published");
        this.retryCounter = meterRegistry.counter("catalog.outbox.retry");
        this.dltCounter = meterRegistry.counter("catalog.outbox.dlt");
        this.cycleTimer = meterRegistry.timer("catalog.outbox.cycle.duration");
    }

    @Scheduled(fixedDelayString = "${catalog.outbox.publisher.fixed-delay:PT2S}")
    public void publicarPendentes() {
        OutboxPublisherResult result = cycleTimer.record(() -> publisherService.publicarLote(settings));
        if (result == null) {
            return;
        }
        claimedCounter.increment(result.claimed());
        publishedCounter.increment(result.published());
        retryCounter.increment(result.retry());
        dltCounter.increment(result.dlt());
        if (result.claimed() > 0) {
            LOGGER.info(
                    "Ciclo outbox concluido: claimed={}, published={}, retry={}, dlt={}",
                    result.claimed(), result.published(), result.retry(), result.dlt());
        }
    }
}
