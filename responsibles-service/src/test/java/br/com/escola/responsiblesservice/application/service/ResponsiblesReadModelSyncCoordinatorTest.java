package br.com.escola.responsiblesservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.escola.responsiblesservice.application.state.ResponsiblesReadModelSyncSummary.TableOperationReport;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesReadModelProperties;

class ResponsiblesReadModelSyncCoordinatorTest {

    @Test
    void deveManterBackfillDesligadoPorPadrao() {
        ResponsiblesReadModelSyncCoordinator coordinator = new ResponsiblesReadModelSyncCoordinator(
                new ResponsiblesReadModelProperties(false, false, false, false, false, 500, false, true),
                (backfillEnabled, reconciliationEnabled, batchSize) -> {
                    throw new AssertionError("sync port should not be called");
                });

        var report = coordinator.executarCicloControlado();

        assertThat(report.status()).isEqualTo("disabled");
        assertThat(report.reason()).isEqualTo("backfill-and-reconciliation-disabled");
        assertThat(report.backfilledRecords()).isZero();
    }

    @Test
    void deveExecutarBackfillControladoDaTabelaResponsavel() {
        ResponsiblesReadModelSyncCoordinator coordinator = new ResponsiblesReadModelSyncCoordinator(
                new ResponsiblesReadModelProperties(true, false, false, true, true, 100, false, true),
                (backfillEnabled, reconciliationEnabled, batchSize) -> List.of(new TableOperationReport(
                        "responsavel",
                        "id_responsavel",
                        "monolith_jdbc",
                        "responsibles_read_model",
                        "success",
                        "responsibles-read-model-backfill-completed",
                        backfillEnabled,
                        reconciliationEnabled,
                        true,
                        1,
                        1,
                        1,
                        0)));

        var report = coordinator.executarCicloControlado();

        assertThat(report.status()).isEqualTo("completed");
        assertThat(report.reason()).isEqualTo("responsibles-read-model-backfill-and-reconciliation-completed");
        assertThat(report.backfilledRecords()).isEqualTo(1);
        assertThat(report.divergentRecords()).isZero();
    }
}
