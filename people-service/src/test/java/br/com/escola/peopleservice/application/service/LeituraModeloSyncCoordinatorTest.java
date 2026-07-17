package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.application.state.LeituraModeloSyncSummary.TableOperationReport;
import br.com.escola.peopleservice.infra.config.LeituraModeloProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class LeituraModeloSyncCoordinatorTest {

    @Test
    void deveManterCicloDesligadoPorPadrao() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        LeituraModeloSyncCoordinator coordinator = new LeituraModeloSyncCoordinator(
                new LeituraModeloProperties(false, false, false, false, false, false, 500, true),
                meterRegistry,
                (backfillEnabled, reconciliationEnabled, batchSize) -> {
                    throw new AssertionError("sync port should not be called");
                },
                new LeituraModeloSyncState());

        var report = coordinator.executarCicloControlado();

        assertThat(report.status()).isEqualTo("disabled");
        assertThat(report.reason()).isEqualTo("operation-flags-disabled");
        assertThat(report.backfillEnabled()).isFalse();
        assertThat(report.reconciliationEnabled()).isFalse();
        assertThat(report.batchSize()).isEqualTo(500);
        assertThat(report.plannedTables()).isEqualTo(14);
        assertThat(report.successfulTables()).isZero();
        assertThat(report.backfilledRecords()).isZero();
        assertThat(report.divergences()).isZero();
        assertThat(report.writesEnabled()).isFalse();
        assertThat(report.localReadRoutingEnabled()).isFalse();
        assertThat(meterRegistry.getMeters()).isEmpty();
    }

    @Test
    void deveExecutarBackfillEReconciliacaoDoReadModelLocalSemEscritaExterna() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        LeituraModeloSyncState state = new LeituraModeloSyncState();
        LeituraModeloSyncCoordinator coordinator = new LeituraModeloSyncCoordinator(
                new LeituraModeloProperties(false, false, false, false, true, true, 100, true),
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
                                0),
                        new TableOperationReport(
                                "status_aluno",
                                "id_status_aluno",
                                "monolith_jdbc",
                                "people_read_model_catalog",
                                "success",
                                "catalog-sync-completed",
                                backfillEnabled,
                                reconciliationEnabled,
                                true,
                                2,
                                2,
                                2,
                                0),
                        new TableOperationReport(
                                "parentesco",
                                "id_parentesco",
                                "monolith_jdbc",
                                "people_read_model_catalog",
                                "success",
                                "catalog-sync-completed",
                                backfillEnabled,
                                reconciliationEnabled,
                                true,
                                2,
                                2,
                                2,
                                0),
                        new TableOperationReport(
                                "pessoa",
                                "id_pessoa",
                                "monolith_jdbc",
                                "people_read_model_identity",
                                "success",
                                "identity-sync-completed",
                                backfillEnabled,
                                reconciliationEnabled,
                                true,
                                2,
                                2,
                                2,
                                0),
                        new TableOperationReport(
                                "pessoa_tipo_pessoa",
                                "id_pessoa_tipo_pessoa",
                                "monolith_jdbc",
                                "people_read_model_identity",
                                "success",
                                "identity-sync-completed",
                                backfillEnabled,
                                reconciliationEnabled,
                                true,
                                3,
                                3,
                                3,
                                0),
                        new TableOperationReport(
                                "aluno",
                                "id_aluno",
                                "monolith_jdbc",
                                "people_read_model_student_responsible",
                                "success",
                                "student-responsible-sync-completed",
                                backfillEnabled,
                                reconciliationEnabled,
                                true,
                                1,
                                1,
                                1,
                                0),
                        new TableOperationReport(
                                "responsavel",
                                "id_responsavel",
                                "monolith_jdbc",
                                "people_read_model_student_responsible",
                                "success",
                                "student-responsible-sync-completed",
                                backfillEnabled,
                                reconciliationEnabled,
                                true,
                                1,
                                1,
                                1,
                                0),
                        new TableOperationReport(
                                "aluno_responsavel",
                                "id_aluno_responsavel",
                                "monolith_jdbc",
                                "people_read_model_student_responsible",
                                "success",
                                "student-responsible-sync-completed",
                                backfillEnabled,
                                reconciliationEnabled,
                                true,
                                1,
                                1,
                                1,
                                0),
                        new TableOperationReport(
                                "endereco",
                                "id_endereco",
                                "monolith_jdbc",
                                "people_read_model_address",
                                "success",
                                "address-sync-completed",
                                backfillEnabled,
                                reconciliationEnabled,
                                true,
                                2,
                                2,
                                2,
                                0),
                        new TableOperationReport(
                                "pessoa_endereco",
                                "id_pessoa_endereco",
                                "monolith_jdbc",
                                "people_read_model_address",
                                "success",
                                "address-sync-completed",
                                backfillEnabled,
                                reconciliationEnabled,
                                true,
                                2,
                                2,
                                2,
                                0),
                        new TableOperationReport(
                                "people_documento_read_model",
                                "id_pessoa_documento",
                                "monolith_jdbc",
                                "people_documento_read_model",
                                "success",
                                "document-metadata-sync-completed",
                                backfillEnabled,
                                reconciliationEnabled,
                                true,
                                2,
                                2,
                                2,
                                0),
                        new TableOperationReport(
                                "people_funcionario_read_model",
                                "id_funcionario",
                                "monolith_jdbc",
                                "people_funcionario_read_model",
                                "success",
                                "funcionario-internal-summary-sync-completed",
                                backfillEnabled,
                                reconciliationEnabled,
                                true,
                                1,
                                1,
                                1,
                                0),
                        new TableOperationReport(
                                "people_professor_read_model",
                                "id_professor",
                                "monolith_jdbc",
                                "people_professor_read_model",
                                "success",
                                "professor-summary-sync-completed",
                                backfillEnabled,
                                reconciliationEnabled,
                                true,
                                1,
                                1,
                                1,
                                0)),
                state);

        var report = coordinator.executarCicloControlado();

        assertThat(report.status()).isEqualTo("completed");
        assertThat(report.reason()).isEqualTo("local-read-model-backfill-and-reconciliation-completed");
        assertThat(report.backfillEnabled()).isTrue();
        assertThat(report.reconciliationEnabled()).isTrue();
        assertThat(report.batchSize()).isEqualTo(100);
        assertThat(report.successfulTables()).isEqualTo(14);
        assertThat(report.backfilledRecords()).isEqualTo(27);
        assertThat(report.sourceRows()).isEqualTo(27);
        assertThat(report.targetRows()).isEqualTo(27);
        assertThat(report.divergences()).isZero();
        assertThat(report.writesEnabled()).isFalse();
        assertThat(report.localReadRoutingEnabled()).isFalse();
        assertThat(report.tables())
                .hasSize(14)
                .allSatisfy(table -> {
                    assertThat(table.source()).isEqualTo("monolith_jdbc");
                    assertThat(table.status()).isEqualTo("success");
                    assertThat(table.backfillPlanned()).isTrue();
                    assertThat(table.reconciliationPlanned()).isTrue();
                    assertThat(table.idempotent()).isTrue();
                });
        assertThat(report.tables())
                .extracting("table")
                .containsExactly(
                        "tipo_pessoa",
                        "tipo_endereco",
                        "status_aluno",
                        "parentesco",
                        "pessoa",
                        "pessoa_tipo_pessoa",
                        "aluno",
                        "responsavel",
                        "aluno_responsavel",
                        "endereco",
                        "pessoa_endereco",
                        "people_documento_read_model",
                        "people_funcionario_read_model",
                        "people_professor_read_model");
        assertThat(state.currentReport()).isEqualTo(report);
        assertThat(meterRegistry.counter("people.readmodel.sync.cycles", "status", "completed").count())
                .isEqualTo(1.0d);
        assertThat(meterRegistry.counter("people.readmodel.sync.tables.planned").count())
                .isEqualTo(14.0d);
        assertThat(meterRegistry.counter("people.readmodel.sync.records").count())
                .isEqualTo(27.0d);
        assertThat(meterRegistry.counter("people.readmodel.sync.reconciliation.tables").count())
                .isEqualTo(14.0d);
    }
}


