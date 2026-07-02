package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.infra.config.PeopleLocalPersistenceProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PeopleLocalPersistenceBackfillCoordinatorTest {

    @Test
    void deveManterCicloDesligadoPorPadrao() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleLocalPersistenceBackfillCoordinator coordinator = new PeopleLocalPersistenceBackfillCoordinator(
                new PeopleLocalPersistenceProperties(false, false, false, false, false, false, 500),
                meterRegistry);

        var report = coordinator.executarCicloControlado();

        assertThat(report.status()).isEqualTo("disabled");
        assertThat(report.reason()).isEqualTo("operation-flags-disabled");
        assertThat(report.backfillEnabled()).isFalse();
        assertThat(report.reconciliationEnabled()).isFalse();
        assertThat(report.batchSize()).isEqualTo(500);
        assertThat(report.plannedTables()).isEqualTo(6);
        assertThat(report.writesEnabled()).isFalse();
        assertThat(report.cutoverEnabled()).isFalse();
        assertThat(meterRegistry.getMeters()).isEmpty();
    }

    @Test
    void devePlanejarBackfillEReconciliacaoSemEscritaLocalOuCutover() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleLocalPersistenceBackfillCoordinator coordinator = new PeopleLocalPersistenceBackfillCoordinator(
                new PeopleLocalPersistenceProperties(false, false, false, false, true, true, 100),
                meterRegistry);

        var report = coordinator.executarCicloControlado();

        assertThat(report.status()).isEqualTo("planned_only");
        assertThat(report.reason()).isEqualTo("local-read-model-storage-not-configured");
        assertThat(report.backfillEnabled()).isTrue();
        assertThat(report.reconciliationEnabled()).isTrue();
        assertThat(report.batchSize()).isEqualTo(100);
        assertThat(report.writesEnabled()).isFalse();
        assertThat(report.cutoverEnabled()).isFalse();
        assertThat(report.tables())
                .hasSize(6)
                .allSatisfy(table -> {
                    assertThat(table.source()).isEqualTo("monolith_proxy");
                    assertThat(table.target()).isEqualTo("people_read_model_candidate");
                    assertThat(table.backfillPlanned()).isTrue();
                    assertThat(table.reconciliationPlanned()).isTrue();
                    assertThat(table.idempotent()).isTrue();
                });
        assertThat(meterRegistry.counter("people.shadow.local.persistence.cycles", "status", "planned_only").count())
                .isEqualTo(1.0d);
        assertThat(meterRegistry.counter("people.shadow.local.persistence.backfill.tables.planned").count())
                .isEqualTo(6.0d);
        assertThat(meterRegistry.counter("people.shadow.local.persistence.reconciliation.tables.planned").count())
                .isEqualTo(6.0d);
    }
}
