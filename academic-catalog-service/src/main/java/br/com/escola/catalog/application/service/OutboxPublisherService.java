package br.com.escola.catalog.application.service;

import java.time.Clock;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.escola.catalog.application.event.BrokerPublication;
import br.com.escola.catalog.application.event.OutboxPublication;
import br.com.escola.catalog.application.port.out.EventBrokerPort;
import br.com.escola.catalog.application.port.out.OutboxPublicationPort;

@Service
public class OutboxPublisherService {

    private final OutboxPublicationPort outboxPort;
    private final EventBrokerPort brokerPort;
    private final Clock clock;

    @Autowired
    public OutboxPublisherService(
            OutboxPublicationPort outboxPort,
            EventBrokerPort brokerPort) {
        this(outboxPort, brokerPort, Clock.systemUTC());
    }

    OutboxPublisherService(
            OutboxPublicationPort outboxPort,
            EventBrokerPort brokerPort,
            Clock clock) {
        this.outboxPort = outboxPort;
        this.brokerPort = brokerPort;
        this.clock = clock;
    }

    public OutboxPublisherResult publicarLote(OutboxPublisherSettings settings) {
        var events = outboxPort.reivindicarLote(settings.batchSize(), settings.lockTimeout());
        int published = 0;
        int retry = 0;
        int dlt = 0;

        for (OutboxPublication event : events) {
            if (event.attempts() >= settings.maxAttempts()) {
                if (publicarDlt(event, settings)) {
                    dlt++;
                } else {
                    retry++;
                }
                continue;
            }

            BrokerPublication publication;
            try {
                publication = brokerPort.publicar(
                        settings.topic(), event.aggregateId().toString(), event.envelope());
            } catch (RuntimeException exception) {
                int nextAttempt = event.attempts() + 1;
                if (nextAttempt >= settings.maxAttempts()) {
                    if (publicarDlt(event, settings, exception)) {
                        dlt++;
                    } else {
                        retry++;
                    }
                } else {
                    outboxPort.marcarRetry(
                            event, message(exception),
                            clock.instant().plus(backoff(settings, nextAttempt)));
                    retry++;
                }
                continue;
            }
            outboxPort.marcarPublicado(event, clock.instant(), publication);
            published++;
        }
        return new OutboxPublisherResult(events.size(), published, retry, dlt);
    }

    private boolean publicarDlt(OutboxPublication event, OutboxPublisherSettings settings) {
        return publicarDlt(event, settings, new IllegalStateException("Limite de tentativas atingido"));
    }

    private boolean publicarDlt(
            OutboxPublication event,
            OutboxPublisherSettings settings,
            RuntimeException originalFailure) {
        try {
            BrokerPublication publication = brokerPort.publicar(
                    settings.dltTopic(), event.aggregateId().toString(), event.envelope());
            outboxPort.marcarDlt(event, message(originalFailure), clock.instant(), publication);
            return true;
        } catch (RuntimeException dltFailure) {
            String combinedError = message(originalFailure) + " | DLT: " + message(dltFailure);
            outboxPort.marcarRetry(
                    event, combinedError,
                    clock.instant().plus(settings.maxBackoff()));
            return false;
        }
    }

    private Duration backoff(OutboxPublisherSettings settings, int attempt) {
        long multiplier = 1L << Math.min(Math.max(attempt - 1, 0), 20);
        Duration calculated;
        try {
            calculated = settings.initialBackoff().multipliedBy(multiplier);
        } catch (ArithmeticException exception) {
            calculated = settings.maxBackoff();
        }
        return calculated.compareTo(settings.maxBackoff()) > 0 ? settings.maxBackoff() : calculated;
    }

    private String message(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }
}
