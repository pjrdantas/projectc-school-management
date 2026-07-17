package br.com.escola.responsiblesservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.escola.responsiblesservice.application.state.ResponsiblesReadModelSyncState;
import br.com.escola.responsiblesservice.application.state.ResponsiblesReadModelSyncSummary;
import br.com.escola.responsiblesservice.application.state.ResponsiblesReadModelSyncSummary.TableOperationReport;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesReadModelProperties;

class ResponsiblesReadModelRouteGuardTest {

    @Test
    void permiteLeituraLocalQuandoReconciliacaoEstaDesligada() {
        ResponsiblesReadModelRouteGuard guard = new ResponsiblesReadModelRouteGuard(
                new ResponsiblesReadModelProperties(true, false, true, false, false, 500, false, true),
                new ResponsiblesReadModelSyncState());

        assertThat(guard.canReadStudentLinksLocally()).isTrue();
    }

    @Test
    void bloqueiaLeituraLocalQuandoReconciliacaoLigadaESemCicloVerde() {
        ResponsiblesReadModelRouteGuard guard = new ResponsiblesReadModelRouteGuard(
                new ResponsiblesReadModelProperties(true, false, true, false, true, 500, false, true),
                new ResponsiblesReadModelSyncState());

        assertThat(guard.canReadStudentLinksLocally()).isFalse();
    }

    @Test
    void liberaLeituraLocalQuandoReconciliacaoLigadaECicloVerde() {
        ResponsiblesReadModelSyncState state = new ResponsiblesReadModelSyncState();
        state.update(summary("completed", 0));
        ResponsiblesReadModelRouteGuard guard = new ResponsiblesReadModelRouteGuard(
                new ResponsiblesReadModelProperties(true, false, true, false, true, 500, false, true),
                state);

        assertThat(guard.canReadStudentLinksLocally()).isTrue();
    }

    private ResponsiblesReadModelSyncSummary summary(String status, int divergentRecords) {
        return new ResponsiblesReadModelSyncSummary(
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
