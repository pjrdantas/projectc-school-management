package br.com.escola.catalog.infra.database.adapter;

import org.springframework.stereotype.Repository;

import br.com.escola.catalog.application.event.IntegrationEventEnvelope;
import br.com.escola.catalog.application.event.OutboxEvent;
import br.com.escola.catalog.application.port.out.IntegrationEventOutboxPort;
import br.com.escola.catalog.infra.database.entity.OutboxEventJpaEntity;
import br.com.escola.catalog.infra.database.repository.OutboxEventJpaRepository;

@Repository
public class IntegrationEventOutboxAdapter implements IntegrationEventOutboxPort {

    private final OutboxEventJpaRepository repository;

    public IntegrationEventOutboxAdapter(OutboxEventJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void adicionar(OutboxEvent event) {
        IntegrationEventEnvelope envelope = event.envelope();
        repository.save(new OutboxEventJpaEntity(
                envelope.eventId(), event.aggregateType(), event.aggregateId(), envelope.eventType(),
                envelope.eventVersion(), envelope.escolaId(), envelope.correlationId(), envelope.causationId(),
                envelope.usuarioId(), envelope.payload(), "PENDENTE", envelope.occurredAt(), null, 0));
    }
}
