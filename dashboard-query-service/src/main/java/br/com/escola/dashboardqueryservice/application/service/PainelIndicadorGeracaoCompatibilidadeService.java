package br.com.escola.dashboardqueryservice.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorSnapshotResponse;
import br.com.escola.dashboardqueryservice.application.exception.PainelQueryServiceResourceNotFoundException;
import br.com.escola.dashboardqueryservice.application.port.in.PainelIndicadorGeracaoCompatibilidadeUseCase;
import br.com.escola.dashboardqueryservice.application.port.in.PainelIndicadorLocalUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.PainelAdministracaoStorePort;
import br.com.escola.dashboardqueryservice.domain.model.PainelPublico;

@Service
@Transactional(readOnly = true)
public class PainelIndicadorGeracaoCompatibilidadeService implements PainelIndicadorGeracaoCompatibilidadeUseCase {

    private static final String PUBLICO_PROFESSOR = "PROFESSOR";

    private final PainelAdministracaoStorePort publicoStore;
    private final PainelIndicadorLocalUseCase indicadorLocalUseCase;

    public PainelIndicadorGeracaoCompatibilidadeService(
            PainelAdministracaoStorePort publicoStore,
            PainelIndicadorLocalUseCase indicadorLocalUseCase) {
        this.publicoStore = publicoStore;
        this.indicadorLocalUseCase = indicadorLocalUseCase;
    }

    @Override
    public List<PainelIndicadorSnapshotResponse> consultarPorPublico(
            InternalRequestContext context, String publicoCodigo, LocalDate referenciaData) {
        PainelPublico publico = publico(context, publicoCodigo);
        return indicadorLocalUseCase.listar(context, publico.id(), dataReferencia(referenciaData));
    }

    @Override
    public List<PainelIndicadorSnapshotResponse> consultarPorProfessor(
            InternalRequestContext context, UUID professorId, LocalDate referenciaData) {
        List<PainelIndicadorSnapshotResponse> snapshots = consultarPorPublico(context, PUBLICO_PROFESSOR, referenciaData);
        String prefixo = "PROFESSOR_" + professorId.toString().replace("-", "").toUpperCase(Locale.ROOT) + "_";
        return snapshots.stream()
                .filter(snapshot -> snapshot.codigoIndicador().startsWith(prefixo))
                .toList();
    }

    private PainelPublico publico(InternalRequestContext context, String publicoCodigo) {
        return publicoStore.buscarPublicoPorCodigo(context.escolaId(), codigo(publicoCodigo))
                .orElseThrow(() -> new PainelQueryServiceResourceNotFoundException("Publico nao encontrado na escola"));
    }

    private LocalDate dataReferencia(LocalDate referenciaData) {
        return referenciaData == null ? LocalDate.now() : referenciaData;
    }

    private String codigo(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
