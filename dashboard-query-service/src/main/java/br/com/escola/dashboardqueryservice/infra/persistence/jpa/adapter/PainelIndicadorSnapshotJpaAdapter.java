package br.com.escola.dashboardqueryservice.infra.persistence.jpa.adapter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.escola.dashboardqueryservice.application.port.out.PainelIndicadorSnapshotStorePort;
import br.com.escola.dashboardqueryservice.domain.model.PainelIndicadorSnapshot;
import br.com.escola.dashboardqueryservice.infra.persistence.jpa.entity.PainelIndicadorSnapshotJpaEntity;
import br.com.escola.dashboardqueryservice.infra.persistence.jpa.repository.PainelIndicadorSnapshotJpaRepository;

@Component
public class PainelIndicadorSnapshotJpaAdapter implements PainelIndicadorSnapshotStorePort {

    private final PainelIndicadorSnapshotJpaRepository repository;

    public PainelIndicadorSnapshotJpaAdapter(PainelIndicadorSnapshotJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PainelIndicadorSnapshot> buscar(UUID escolaId, UUID snapshotId) {
        return repository.findByIdAndEscolaId(snapshotId, escolaId).map(this::toModel);
    }

    @Override
    public Optional<PainelIndicadorSnapshot> buscarPorChave(
            UUID escolaId, UUID publicoId, String codigoIndicador, LocalDate referenciaData) {
        return repository.findByEscolaIdAndPublicoIdAndCodigoIndicadorAndReferenciaData(
                escolaId, publicoId, codigoIndicador, referenciaData).map(this::toModel);
    }

    @Override
    public List<PainelIndicadorSnapshot> listar(UUID escolaId, UUID publicoId, LocalDate referenciaData) {
        List<PainelIndicadorSnapshotJpaEntity> entities = referenciaData == null
                ? repository.findByEscolaIdAndPublicoIdOrderByCodigoIndicadorAscReferenciaDataAsc(escolaId, publicoId)
                : repository.findByEscolaIdAndPublicoIdAndReferenciaDataOrderByCodigoIndicadorAsc(
                        escolaId, publicoId, referenciaData);
        return entities.stream().map(this::toModel).toList();
    }

    @Override
    public List<PainelIndicadorSnapshot> listarHistorico(UUID escolaId, UUID publicoId) {
        return repository.findByEscolaIdAndPublicoIdOrderByCodigoIndicadorAscReferenciaDataAsc(escolaId, publicoId)
                .stream().map(this::toModel).toList();
    }

    @Override
    public PainelIndicadorSnapshot salvar(PainelIndicadorSnapshot snapshot) {
        LocalDateTime now = LocalDateTime.now();
        PainelIndicadorSnapshotJpaEntity entity = repository.findByEscolaIdAndPublicoIdAndCodigoIndicadorAndReferenciaData(
                snapshot.escolaId(), snapshot.publicoId(), snapshot.codigoIndicador(), snapshot.referenciaData())
                .orElseGet(() -> new PainelIndicadorSnapshotJpaEntity(snapshot.id(), snapshot.escolaId(), snapshot.publicoId(),
                        snapshot.codigoIndicador(), snapshot.descricao(), snapshot.valorNumeric(), snapshot.valorTexto(),
                        snapshot.escolaNome(), snapshot.referenciaData(), now, now));
        entity.atualizar(snapshot.descricao(), snapshot.valorNumeric(), snapshot.valorTexto(), snapshot.escolaNome(), now);
        return toModel(repository.save(entity));
    }

    @Override
    public void excluir(PainelIndicadorSnapshot snapshot) {
        repository.deleteById(snapshot.id());
    }

    private PainelIndicadorSnapshot toModel(PainelIndicadorSnapshotJpaEntity entity) {
        return new PainelIndicadorSnapshot(entity.getId(), entity.getEscolaId(), entity.getPublicoId(),
                entity.getCodigoIndicador(), entity.getDescricao(), entity.getValorNumeric(), entity.getValorTexto(),
                entity.getEscolaNome(), entity.getReferenciaData());
    }
}
