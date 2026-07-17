package br.com.escola.responsiblesservice.infra.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.escola.responsiblesservice.application.state.ResponsiblesReadModelSyncState;
import br.com.escola.responsiblesservice.application.state.ResponsiblesReadModelSyncSummary;
import br.com.escola.responsiblesservice.application.state.ResponsiblesReadModelSyncSummary.TableOperationReport;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesReadModelProperties;

class ResponsiblesReadModelHealthIndicatorTest {

    @Test
    void expõeHealthUpQuandoRotaLocalEstaPronta() {
        ResponsiblesReadModelSyncState state = new ResponsiblesReadModelSyncState();
        state.update(summary("completed", 0));
        ResponsiblesReadModelHealthIndicator indicator = new ResponsiblesReadModelHealthIndicator(
                new ResponsiblesReadModelProperties(true, false, true, true, true, 500, false, true),
                state);

        var health = indicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("UP");
        assertThat(health.getDetails()).containsEntry("linkRouteReady", true);
        assertThat(health.getDetails()).containsEntry("divergentRecords", 0);
    }

    @Test
    void expõeHealthUnknownQuandoReconciliacaoAindaNaoEstaVerde() {
        ResponsiblesReadModelSyncState state = new ResponsiblesReadModelSyncState();
        state.update(summary("blocked", 2));
        ResponsiblesReadModelHealthIndicator indicator = new ResponsiblesReadModelHealthIndicator(
                new ResponsiblesReadModelProperties(true, false, true, true, true, 500, false, true),
                state);

        var health = indicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("UNKNOWN");
        assertThat(health.getDetails()).containsEntry("linkRouteReady", false);
        assertThat(health.getDetails()).containsEntry("divergentRecords", 2);
    }

    private ResponsiblesReadModelSyncSummary summary(String status, int divergentRecords) {
        return new ResponsiblesReadModelSyncSummary(
                true,
                true,
                status,
                "ok",
                100,
                3,
                status.equals("completed") ? 3 : 0,
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
