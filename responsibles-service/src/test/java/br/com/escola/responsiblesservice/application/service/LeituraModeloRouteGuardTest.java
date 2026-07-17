package br.com.escola.responsiblesservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.escola.responsiblesservice.application.state.LeituraModeloSyncState;
import br.com.escola.responsiblesservice.application.state.LeituraModeloSyncSummary;
import br.com.escola.responsiblesservice.application.state.LeituraModeloSyncSummary.TableOperationReport;
import br.com.escola.responsiblesservice.infra.config.LeituraModeloProperties;

class LeituraModeloRouteGuardTest {

    @Test
    void permiteCatalogoLocalQuandoReconciliacaoLigadaEResponsavelEstaVerde() {
        LeituraModeloSyncState state = new LeituraModeloSyncState();
        state.update(summaryWithCatalogReadyAndLinksDivergent());
        LeituraModeloRouteGuard guard = new LeituraModeloRouteGuard(
                new LeituraModeloProperties(true, false, true, false, true, 500, false, true),
                state);

        assertThat(guard.canReadCatalogLocally()).isTrue();
        assertThat(guard.canReadStudentLinksLocally()).isFalse();
    }

    @Test
    void permiteLeituraLocalQuandoReconciliacaoEstaDesligada() {
        LeituraModeloRouteGuard guard = new LeituraModeloRouteGuard(
                new LeituraModeloProperties(true, false, true, false, false, 500, false, true),
                new LeituraModeloSyncState());

        assertThat(guard.canReadCatalogLocally()).isTrue();
        assertThat(guard.canReadStudentLinksLocally()).isTrue();
    }

    @Test
    void bloqueiaLeituraLocalQuandoReconciliacaoLigadaESemCicloVerde() {
        LeituraModeloRouteGuard guard = new LeituraModeloRouteGuard(
                new LeituraModeloProperties(true, false, true, false, true, 500, false, true),
                new LeituraModeloSyncState());

        assertThat(guard.canReadCatalogLocally()).isFalse();
        assertThat(guard.canReadStudentLinksLocally()).isFalse();
    }

    @Test
    void liberaLeituraLocalQuandoReconciliacaoLigadaECicloVerde() {
        LeituraModeloSyncState state = new LeituraModeloSyncState();
        state.update(summary("completed", 0));
        LeituraModeloRouteGuard guard = new LeituraModeloRouteGuard(
                new LeituraModeloProperties(true, false, true, false, true, 500, false, true),
                state);

        assertThat(guard.canReadCatalogLocally()).isTrue();
        assertThat(guard.canReadStudentLinksLocally()).isTrue();
    }

    private LeituraModeloSyncSummary summaryWithCatalogReadyAndLinksDivergent() {
        return new LeituraModeloSyncSummary(
                true,
                true,
                "blocked",
                "responsibles-read-model-reconciliation-divergent",
                100,
                3,
                1,
                1,
                2,
                3,
                1,
                List.of(
                        table("responsavel", 0),
                        table("parentesco", 1),
                        table("aluno_responsavel", 1)));
    }

    private LeituraModeloSyncSummary summary(String status, int divergentRecords) {
        return new LeituraModeloSyncSummary(
                true,
                true,
                status,
                "ok",
                100,
                3,
                3,
                3,
                divergentRecords,
                3,
                3,
                List.of(
                        table("responsavel", divergentRecords),
                        table("parentesco", divergentRecords),
                        table("aluno_responsavel", divergentRecords)));
    }

    private TableOperationReport table(String table, int divergentRecords) {
        return new TableOperationReport(
                table,
                "id",
                "source",
                "target",
                divergentRecords == 0 ? "success" : "divergent",
                divergentRecords == 0 ? "ok" : "divergent",
                true,
                true,
                true,
                1,
                1,
                1,
                divergentRecords);
    }
}

