package br.com.escola.catalog.infra.database.adapter;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.catalog.application.event.BrokerPublication;
import br.com.escola.catalog.application.event.IntegrationEventEnvelope;
import br.com.escola.catalog.application.event.OutboxPublication;
import br.com.escola.catalog.application.port.out.OutboxPublicationPort;
import br.com.escola.catalog.infra.database.entity.OutboxEventJpaEntity;
import br.com.escola.catalog.infra.database.repository.OutboxEventJpaRepository;

@Repository
public class OutboxPublicationAdapter implements OutboxPublicationPort {

    private static final String CLAIM_SQL = """
            WITH candidates AS (
                SELECT id_evento
                  FROM outbox_event
                 WHERE status IN ('PENDENTE', 'RETRY')
                   AND (next_attempt_at IS NULL OR next_attempt_at <= CURRENT_TIMESTAMP)
                   AND (locked_at IS NULL
                        OR locked_at < CURRENT_TIMESTAMP - (? * INTERVAL '1 second'))
                 ORDER BY occurred_at, id_evento
                 LIMIT ?
                 FOR UPDATE SKIP LOCKED
            )
            UPDATE outbox_event event
               SET locked_at = CURRENT_TIMESTAMP
              FROM candidates
             WHERE event.id_evento = candidates.id_evento
            RETURNING event.id_evento, event.locked_at
            """;

    private final JdbcTemplate jdbcTemplate;
    private final OutboxEventJpaRepository repository;

    public OutboxPublicationAdapter(JdbcTemplate jdbcTemplate, OutboxEventJpaRepository repository) {
        this.jdbcTemplate = jdbcTemplate;
        this.repository = repository;
    }

    @Override
    @Transactional
    public List<OutboxPublication> reivindicarLote(int batchSize, Duration lockTimeout) {
        Map<UUID, Instant> claims = jdbcTemplate.query(
                CLAIM_SQL,
                (resultSet, rowNumber) -> Map.entry(
                        resultSet.getObject("id_evento", UUID.class),
                        resultSet.getTimestamp("locked_at").toInstant()),
                Math.max(lockTimeout.toSeconds(), 1),
                batchSize).stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> left,
                        LinkedHashMap::new));

        Map<UUID, OutboxEventJpaEntity> entities = repository.findAllById(claims.keySet()).stream()
                .collect(Collectors.toMap(OutboxEventJpaEntity::getId, Function.identity()));
        return claims.entrySet().stream()
                .map(entry -> toPublication(entities.get(entry.getKey()), entry.getValue()))
                .toList();
    }

    @Override
    public void marcarPublicado(
            OutboxPublication event,
            Instant publishedAt,
            BrokerPublication publication) {
        int updated = jdbcTemplate.update("""
                UPDATE outbox_event
                   SET status = 'PUBLICADO', attempts = attempts + 1,
                       published_at = ?, next_attempt_at = NULL, locked_at = NULL,
                       last_error = NULL, broker_topic = ?, broker_partition = ?, broker_offset = ?
                 WHERE id_evento = ? AND locked_at = ?
                   AND status IN ('PENDENTE', 'RETRY')
                """,
                Timestamp.from(publishedAt), publication.topic(), publication.partition(), publication.offset(),
                event.envelope().eventId(), Timestamp.from(event.claimToken()));
        requireClaim(updated, event);
    }

    @Override
    public void marcarRetry(OutboxPublication event, String error, Instant nextAttemptAt) {
        int updated = jdbcTemplate.update("""
                UPDATE outbox_event
                   SET status = 'RETRY', attempts = attempts + 1,
                       next_attempt_at = ?, locked_at = NULL, last_error = ?,
                       broker_topic = NULL, broker_partition = NULL, broker_offset = NULL
                 WHERE id_evento = ? AND locked_at = ?
                   AND status IN ('PENDENTE', 'RETRY')
                """,
                Timestamp.from(nextAttemptAt), truncate(error),
                event.envelope().eventId(), Timestamp.from(event.claimToken()));
        requireClaim(updated, event);
    }

    @Override
    public void marcarDlt(
            OutboxPublication event,
            String error,
            Instant publishedAt,
            BrokerPublication publication) {
        int updated = jdbcTemplate.update("""
                UPDATE outbox_event
                   SET status = 'DLT', attempts = attempts + 1,
                       published_at = ?, next_attempt_at = NULL, locked_at = NULL,
                       last_error = ?, broker_topic = ?, broker_partition = ?, broker_offset = ?
                 WHERE id_evento = ? AND locked_at = ?
                   AND status IN ('PENDENTE', 'RETRY')
                """,
                Timestamp.from(publishedAt), truncate(error), publication.topic(),
                publication.partition(), publication.offset(), event.envelope().eventId(),
                Timestamp.from(event.claimToken()));
        requireClaim(updated, event);
    }

    private OutboxPublication toPublication(OutboxEventJpaEntity entity, Instant claimToken) {
        if (entity == null) {
            throw new IllegalStateException("Evento reivindicado nao foi encontrado");
        }
        IntegrationEventEnvelope envelope = new IntegrationEventEnvelope(
                entity.getId(), entity.getEventType(), entity.getEventVersion(), entity.getOccurredAt(),
                entity.getCorrelationId(), entity.getCausationId(), entity.getUsuarioId(),
                entity.getEscolaId(), entity.getPayload());
        return new OutboxPublication(
                entity.getAggregateType(), entity.getAggregateId(), envelope, entity.getAttempts(), claimToken);
    }

    private void requireClaim(int updated, OutboxPublication event) {
        if (updated != 1) {
            throw new IllegalStateException(
                    "Claim da outbox expirou para o evento " + event.envelope().eventId());
        }
    }

    private String truncate(String error) {
        String value = error == null ? "Erro de publicacao sem mensagem" : error;
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}
