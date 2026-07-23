package br.com.escola.dashboardqueryservice.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.dashboardqueryservice.domain.model.PainelIndicadorSnapshot;

public interface PainelIndicadorSnapshotStorePort {

    Optional<PainelIndicadorSnapshot> buscar(UUID escolaId, UUID snapshotId);

    Optional<PainelIndicadorSnapshot> buscarPorChave(
            UUID escolaId, UUID publicoId, String codigoIndicador, LocalDate referenciaData);

    List<PainelIndicadorSnapshot> listar(UUID escolaId, UUID publicoId, LocalDate referenciaData);

    List<PainelIndicadorSnapshot> listarHistorico(UUID escolaId, UUID publicoId);

    PainelIndicadorSnapshot salvar(PainelIndicadorSnapshot snapshot);

    void excluir(PainelIndicadorSnapshot snapshot);
}
