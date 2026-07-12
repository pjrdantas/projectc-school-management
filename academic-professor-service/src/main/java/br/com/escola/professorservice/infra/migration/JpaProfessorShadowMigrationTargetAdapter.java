package br.com.escola.professorservice.infra.migration;

import static java.util.stream.Collectors.toMap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.professorservice.application.migration.ProfessorShadowMigrationSnapshot;
import br.com.escola.professorservice.application.port.out.ProfessorShadowMigrationTargetPort;
import br.com.escola.professorservice.infra.database.entity.ProfessorShadowJpaEntity;
import br.com.escola.professorservice.infra.database.entity.ProfessorShadowSyncStateJpaEntity;
import br.com.escola.professorservice.infra.database.repository.ProfessorShadowJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorShadowSyncStateJpaRepository;

@Component
@Transactional
@ConditionalOnProperty(name = "professor.shadow.migration.enabled", havingValue = "true")
public class JpaProfessorShadowMigrationTargetAdapter implements ProfessorShadowMigrationTargetPort {

    private final ProfessorShadowJpaRepository repository;
    private final ProfessorShadowSyncStateJpaRepository syncStateRepository;

    public JpaProfessorShadowMigrationTargetAdapter(
            ProfessorShadowJpaRepository repository,
            ProfessorShadowSyncStateJpaRepository syncStateRepository) {
        this.repository = repository;
        this.syncStateRepository = syncStateRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfessorShadowMigrationSnapshot carregarSnapshot() {
        return new ProfessorShadowMigrationSnapshot(repository.findAll().stream()
                .map(this::toRow)
                .sorted(java.util.Comparator.comparing(ProfessorShadowMigrationSnapshot.ProfessorRow::escolaId)
                        .thenComparing(ProfessorShadowMigrationSnapshot.ProfessorRow::nomeCompleto)
                        .thenComparing(ProfessorShadowMigrationSnapshot.ProfessorRow::id))
                .toList());
    }

    @Override
    public void aplicar(ProfessorShadowMigrationSnapshot snapshot) {
        Map<UUID, ProfessorShadowJpaEntity> existing = repository.findAll().stream()
                .collect(toMap(ProfessorShadowJpaEntity::getId, entity -> entity));
        List<ProfessorShadowJpaEntity> entities = snapshot.professores().stream()
                .map(row -> toEntity(existing.get(row.id()), row))
                .toList();
        repository.saveAll(entities);
        syncStateRepository.saveAll(snapshot.professores().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        ProfessorShadowMigrationSnapshot.ProfessorRow::escolaId,
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .map(entry -> new ProfessorShadowSyncStateJpaEntity(
                        entry.getKey(),
                        true,
                        entry.getValue(),
                        LocalDateTime.now(java.time.Clock.systemUTC())))
                .toList());
    }

    private ProfessorShadowMigrationSnapshot.ProfessorRow toRow(ProfessorShadowJpaEntity entity) {
        return new ProfessorShadowMigrationSnapshot.ProfessorRow(
                entity.getId(),
                entity.getPessoaId(),
                entity.getEscolaId(),
                entity.getEscolaNome(),
                entity.getNomeCompleto(),
                entity.getRegistroProfissional(),
                entity.getFormacao(),
                Boolean.TRUE.equals(entity.getAtivo()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getUsuarioId());
    }

    private ProfessorShadowJpaEntity toEntity(
            ProfessorShadowJpaEntity existing,
            ProfessorShadowMigrationSnapshot.ProfessorRow row) {
        return new ProfessorShadowJpaEntity(
                row.id(),
                row.pessoaId(),
                row.nomeCompleto(),
                row.escolaId(),
                row.escolaNome(),
                row.registroProfissional(),
                row.formacao(),
                row.ativo(),
                row.createdAt(),
                row.updatedAt(),
                row.usuarioId());
    }
}
