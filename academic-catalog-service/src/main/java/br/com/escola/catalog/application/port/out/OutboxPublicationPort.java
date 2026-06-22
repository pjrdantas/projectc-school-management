package br.com.escola.catalog.application.port.out;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import br.com.escola.catalog.application.event.BrokerPublication;
import br.com.escola.catalog.application.event.OutboxPublication;

public interface OutboxPublicationPort {

    List<OutboxPublication> reivindicarLote(int batchSize, Duration lockTimeout);

    void marcarPublicado(OutboxPublication event, Instant publishedAt, BrokerPublication publication);

    void marcarRetry(OutboxPublication event, String error, Instant nextAttemptAt);

    void marcarDlt(OutboxPublication event, String error, Instant publishedAt, BrokerPublication publication);
}
