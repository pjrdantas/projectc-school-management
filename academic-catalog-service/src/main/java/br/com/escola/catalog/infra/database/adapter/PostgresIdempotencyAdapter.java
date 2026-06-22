package br.com.escola.catalog.infra.database.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import br.com.escola.catalog.application.idempotency.CommandIdempotency;
import br.com.escola.catalog.application.port.out.IdempotencyPort;
import br.com.escola.catalog.infra.database.entity.CommandIdempotencyJpaEntity;
import br.com.escola.catalog.infra.database.repository.CommandIdempotencyJpaRepository;

@Repository
public class PostgresIdempotencyAdapter implements IdempotencyPort {

    private final JdbcTemplate jdbcTemplate;
    private final CommandIdempotencyJpaRepository repository;

    public PostgresIdempotencyAdapter(
            JdbcTemplate jdbcTemplate,
            CommandIdempotencyJpaRepository repository) {
        this.jdbcTemplate = jdbcTemplate;
        this.repository = repository;
    }

    @Override
    public void bloquear(UUID escolaId, String key) {
        jdbcTemplate.queryForObject(
                "SELECT pg_advisory_xact_lock(hashtextextended(?, 0))",
                (resultSet, rowNumber) -> Boolean.TRUE,
                escolaId + ":" + key);
    }

    @Override
    public Optional<CommandIdempotency> buscar(UUID escolaId, String key) {
        return repository.findByEscolaIdAndKey(escolaId, key).map(this::toDomain);
    }

    @Override
    public void salvar(CommandIdempotency idempotency) {
        repository.save(new CommandIdempotencyJpaEntity(
                UUID.randomUUID(), idempotency.escolaId(), idempotency.key(), idempotency.requestHash(),
                idempotency.resourceType(), idempotency.resourceId(), idempotency.createdAt()));
    }

    private CommandIdempotency toDomain(CommandIdempotencyJpaEntity entity) {
        return new CommandIdempotency(
                entity.getEscolaId(), entity.getKey(), entity.getRequestHash(),
                entity.getResourceType(), entity.getResourceId(), entity.getCreatedAt());
    }
}
