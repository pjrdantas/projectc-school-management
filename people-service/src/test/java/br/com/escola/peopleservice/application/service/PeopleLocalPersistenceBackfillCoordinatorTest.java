package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.application.dto.PeopleLocalPersistenceOperationReport.TableOperationReport;
import br.com.escola.peopleservice.infra.config.PeopleLocalPersistenceProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PeopleLocalPersistenceBackfillCoordinatorTest {

    @Test
    void deveManterCicloDesligadoPorPadrao() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleLocalPersistenceBackfillCoordinator coordinator = new PeopleLocalPersistenceBackfillCoordinator(
                new PeopleLocalPersistenceProperties(false, false, false, false, false, false, 500, true),
                meterRegistry,
                (backfillEnabled, reconciliationEnabled, batchSize) -> {
                    throw new AssertionError("sync port should not be called");
                },
                new PeopleLocalPersistenceOperationState());

        var report = coordinator.executarCicloControlado();

        assertThat(report.status()).isEqualTo("disabled");
        assertThat(report.reason()).isEqualTo("operation-flags-disabled");
        assertThat(report.backfillEnabled()).isFalse();
        assertThat(report.reconciliationEnabled()).isFalse();
        assertThat(report.batchSize()).isEqualTo(500);
        assertThat(report.plannedTables()).isEqualTo(2);
        assertThat(report.successfulTables()).isZero();
        assertThat(report.backfilledRecords()).isZero();
        assertThat(report.divergences()).isZero();
        assertThat(report.writesEnabled()).isFalse();
        assertThat(report.cutoverEnabled()).isFalse();
        assertThat(meterRegistry.getMeters()).isEmpty();
    }

    @Test
    void deveExecutarBackfillEReconciliacaoDeCatalogosSemEscritaExternaOuCutover() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleLocalPersistenceOperationState state = new PeopleLocalPersistenceOperationState();
        PeopleLocalPersistenceBackfillCoordinator coordinator = new PeopleLocalPersistenceBackfillCoordinator(
                new PeopleLocalPersistenceProperties(false, false, false, false, true, true, 100, true),
                meterRegistry,
                (backfillEnabled, reconciliationEnabled, batchSize) -> List.of(
                        new TableOperationReport(
                                "tipo_pessoa",
                                "id_tipo_pessoa",
                                "monolith_jdbc",
                                "people_read_model_catalog",
                                "success",
                                "catalog-sync-completed",
                                backfillEnabled,
                                reconciliationEnabled,
                                true,
                                4,
                                4,
                                4,
                                0),
                        new TableOperationReport(
                                "tipo_endereco",
                                "id_tipo_endereco",
                                "monolith_jdbc",
                                "people_read_model_catalog",
                                "success",
                                "catalog-sync-completed",
                                backfillEnabled,
                                reconciliationEnabled,
                                true,
                                3,
                                3,
                                3,
                                0)),
                state);

        var report = coordinator.executarCicloControlado();

        assertThat(report.status()).isEqualTo("completed");
        assertThat(report.reason()).isEqualTo("catalog-backfill-and-reconciliation-completed");
        assertThat(report.backfillEnabled()).isTrue();
        assertThat(report.reconciliationEnabled()).isTrue();
        assertThat(report.batchSize()).isEqualTo(100);
        assertThat(report.successfulTables()).isEqualTo(2);
        assertThat(report.backfilledRecords()).isEqualTo(7);
        assertThat(report.sourceRows()).isEqualTo(7);
        assertThat(report.targetRows()).isEqualTo(7);
        assertThat(report.divergences()).isZero();
        assertThat(report.writesEnabled()).isFalse();
        assertThat(report.cutoverEnabled()).isFalse();
        assertThat(report.tables())
                .hasSize(2)
                .allSatisfy(table -> {
                    assertThat(table.source()).isEqualTo("monolith_jdbc");
                    assertThat(table.target()).isEqualTo("people_read_model_catalog");
                    assertThat(table.status()).isEqualTo("success");
                    assertThat(table.backfillPlanned()).isTrue();
                    assertThat(table.reconciliationPlanned()).isTrue();
                    assertThat(table.idempotent()).isTrue();
                });
        assertThat(state.currentReport()).isEqualTo(report);
        assertThat(meterRegistry.counter("people.shadow.local.persistence.cycles", "status", "completed").count())
                .isEqualTo(1.0d);
        assertThat(meterRegistry.counter("people.shadow.local.persistence.backfill.tables.planned").count())
                .isEqualTo(2.0d);
        assertThat(meterRegistry.counter("people.shadow.local.persistence.backfill.records").count())
                .isEqualTo(7.0d);
        assertThat(meterRegistry.counter("people.shadow.local.persistence.reconciliation.tables.planned").count())
                .isEqualTo(2.0d);
    }
}
