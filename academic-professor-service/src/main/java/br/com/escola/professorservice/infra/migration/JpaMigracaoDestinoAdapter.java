package br.com.escola.professorservice.infra.migration;

import static java.util.stream.Collectors.toMap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.professorservice.application.migration.MigracaoSnapshot;
import br.com.escola.professorservice.application.port.out.MigracaoDestinoPort;
import br.com.escola.professorservice.infra.database.entity.CadastroJpaEntity;
import br.com.escola.professorservice.infra.database.entity.CadastroSyncStateJpaEntity;
import br.com.escola.professorservice.infra.database.repository.CadastroJpaRepository;
import br.com.escola.professorservice.infra.database.repository.CadastroSyncStateJpaRepository;

@Component
@Transactional
@ConditionalOnProperty(name = "professor.shadow.migration.enabled", havingValue = "true")
public class JpaMigracaoDestinoAdapter implements MigracaoDestinoPort {

    private final CadastroJpaRepository repository;
    private final CadastroSyncStateJpaRepository syncStateRepository;

    public JpaMigracaoDestinoAdapter(
            CadastroJpaRepository repository,
            CadastroSyncStateJpaRepository syncStateRepository) {
        this.repository = repository;
        this.syncStateRepository = syncStateRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public MigracaoSnapshot carregarSnapshot() {
        return new MigracaoSnapshot(repository.findAll().stream()
                .map(this::toRow)
                .sorted(java.util.Comparator.comparing(MigracaoSnapshot.CadastroRow::escolaId)
                        .thenComparing(MigracaoSnapshot.CadastroRow::nomeCompleto)
                        .thenComparing(MigracaoSnapshot.CadastroRow::id))
                .toList());
    }

    @Override
    public void aplicar(MigracaoSnapshot snapshot) {
        Map<UUID, CadastroJpaEntity> existing = repository.findAll().stream()
                .collect(toMap(CadastroJpaEntity::getId, entity -> entity));
        List<CadastroJpaEntity> entities = snapshot.professores().stream()
                .map(row -> toEntity(existing.get(row.id()), row))
                .toList();
        repository.saveAll(entities);
        syncStateRepository.saveAll(snapshot.professores().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        MigracaoSnapshot.CadastroRow::escolaId,
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .map(entry -> new CadastroSyncStateJpaEntity(
                        entry.getKey(),
                        true,
                        entry.getValue(),
                        LocalDateTime.now(java.time.Clock.systemUTC())))
                .toList());
    }

    private MigracaoSnapshot.CadastroRow toRow(CadastroJpaEntity entity) {
        return new MigracaoSnapshot.CadastroRow(
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

    private CadastroJpaEntity toEntity(
            CadastroJpaEntity existing,
            MigracaoSnapshot.CadastroRow row) {
        return new CadastroJpaEntity(
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


