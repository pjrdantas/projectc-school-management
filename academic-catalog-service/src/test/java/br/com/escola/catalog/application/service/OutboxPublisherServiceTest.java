package br.com.escola.catalog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.escola.catalog.application.event.BrokerPublication;
import br.com.escola.catalog.application.event.IntegrationEventEnvelope;
import br.com.escola.catalog.application.event.OutboxPublication;
import br.com.escola.catalog.application.port.out.EventBrokerPort;
import br.com.escola.catalog.application.port.out.OutboxPublicationPort;

class OutboxPublisherServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-22T12:00:00Z");
    private static final String TOPIC = "school.catalog.events.v1";
    private static final String DLT_TOPIC = TOPIC + ".DLT";

    @Mock
    private OutboxPublicationPort outboxPort;

    @Mock
    private EventBrokerPort brokerPort;

    private OutboxPublisherService service;
    private OutboxPublisherSettings settings;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OutboxPublisherService(outboxPort, brokerPort, Clock.fixed(NOW, ZoneOffset.UTC));
        settings = new OutboxPublisherSettings(
                10, 3, Duration.ofMinutes(1), Duration.ofSeconds(5),
                Duration.ofMinutes(1), TOPIC, DLT_TOPIC);
    }

    @Test
    void deveMarcarPublicadoSomenteDepoisDoBrokerConfirmar() {
        OutboxPublication event = event(0);
        BrokerPublication metadata = new BrokerPublication(TOPIC, 1, 42L);
        when(outboxPort.reivindicarLote(10, Duration.ofMinutes(1))).thenReturn(List.of(event));
        when(brokerPort.publicar(TOPIC, event.aggregateId().toString(), event.envelope()))
                .thenReturn(metadata);

        OutboxPublisherResult result = service.publicarLote(settings);

        var order = inOrder(brokerPort, outboxPort);
        order.verify(brokerPort).publicar(TOPIC, event.aggregateId().toString(), event.envelope());
        order.verify(outboxPort).marcarPublicado(event, NOW, metadata);
        assertThat(result).isEqualTo(new OutboxPublisherResult(1, 1, 0, 0));
    }

    @Test
    void deveAgendarRetryComBackoffAposFalhaDoBroker() {
        OutboxPublication event = event(0);
        when(outboxPort.reivindicarLote(10, Duration.ofMinutes(1))).thenReturn(List.of(event));
        when(brokerPort.publicar(TOPIC, event.aggregateId().toString(), event.envelope()))
                .thenThrow(new IllegalStateException("broker indisponivel"));

        OutboxPublisherResult result = service.publicarLote(settings);

        verify(outboxPort).marcarRetry(event, "broker indisponivel", NOW.plusSeconds(5));
        verify(outboxPort, never()).marcarPublicado(eq(event), eq(NOW), org.mockito.ArgumentMatchers.any());
        assertThat(result).isEqualTo(new OutboxPublisherResult(1, 0, 1, 0));
    }

    @Test
    void deveEnviarParaDltQuandoLimiteForAtingido() {
        OutboxPublication event = event(2);
        BrokerPublication dltMetadata = new BrokerPublication(DLT_TOPIC, 0, 7L);
        when(outboxPort.reivindicarLote(10, Duration.ofMinutes(1))).thenReturn(List.of(event));
        when(brokerPort.publicar(TOPIC, event.aggregateId().toString(), event.envelope()))
                .thenThrow(new IllegalStateException("falha final"));
        when(brokerPort.publicar(DLT_TOPIC, event.aggregateId().toString(), event.envelope()))
                .thenReturn(dltMetadata);

        OutboxPublisherResult result = service.publicarLote(settings);

        verify(outboxPort).marcarDlt(event, "falha final", NOW, dltMetadata);
        assertThat(result).isEqualTo(new OutboxPublisherResult(1, 0, 0, 1));
    }

    @Test
    void deveManterRetryQuandoAPropriaDltFalhar() {
        OutboxPublication event = event(3);
        when(outboxPort.reivindicarLote(10, Duration.ofMinutes(1))).thenReturn(List.of(event));
        when(brokerPort.publicar(DLT_TOPIC, event.aggregateId().toString(), event.envelope()))
                .thenThrow(new IllegalStateException("dlt indisponivel"));

        OutboxPublisherResult result = service.publicarLote(settings);

        verify(brokerPort, never()).publicar(TOPIC, event.aggregateId().toString(), event.envelope());
        verify(outboxPort).marcarRetry(
                event,
                "Limite de tentativas atingido | DLT: dlt indisponivel",
                NOW.plus(Duration.ofMinutes(1)));
        assertThat(result).isEqualTo(new OutboxPublisherResult(1, 0, 1, 0));
    }

    private OutboxPublication event(int attempts) {
        UUID eventId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        IntegrationEventEnvelope envelope = new IntegrationEventEnvelope(
                eventId, "subject-created", 1, NOW.minusSeconds(30), "corr-test",
                null, UUID.randomUUID(), UUID.randomUUID(), Map.of("disciplinaId", aggregateId));
        return new OutboxPublication(
                "DISCIPLINA", aggregateId, envelope, attempts, NOW.minusSeconds(1));
    }
}
