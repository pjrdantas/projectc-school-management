package br.com.escola.peopleservice.infra.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import br.com.escola.peopleservice.application.service.PeopleLocalPersistenceOperationState;
import br.com.escola.peopleservice.application.service.PeopleLocalReadModelSchemaMigrationState;
import br.com.escola.peopleservice.infra.config.PeopleLocalPersistenceProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PeopleLocalPersistenceHealthIndicatorTest {

    @Test
    void deveReportarFundacaoLocalDesligadaPorPadrao() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        Counter.builder("people.shadow.local.persistence.backfill.records")
                .tag("tabela", "pessoa")
                .tag("resultado", "success")
                .register(meterRegistry)
                .increment(3.0d);
        Counter.builder("people.shadow.local.persistence.reconciliation.divergences")
                .tag("tabela", "pessoa")
                .tag("tipo", "cpf")
                .register(meterRegistry)
                .increment();

        PeopleLocalPersistenceHealthIndicator indicator = new PeopleLocalPersistenceHealthIndicator(
                new PeopleLocalPersistenceProperties(false, false, false, false, false, false, 500, true),
                meterRegistry,
                new br.com.escola.peopleservice.application.service.PeopleLocalReadCutoverGuard(
                        new PeopleLocalPersistenceProperties(false, false, false, false, false, false, 500, true),
                        meterRegistry,
                        new PeopleLocalPersistenceOperationState()),
                new br.com.escola.peopleservice.application.service.PeopleCatalogReadModelSchemaPlanner(),
                new br.com.escola.peopleservice.application.service.PeopleTransactionalReadModelExpansionPlanner(),
                new PeopleLocalReadModelSchemaMigrationState(),
                new PeopleLocalPersistenceOperationState());

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("enabled", false)
                .containsEntry("migrationEnabled", false)
                .containsEntry("readModelCutoverEnabled", false)
                .containsEntry("backfillEnabled", false)
                .containsEntry("reconciliationEnabled", false)
                .containsEntry("backfillBatchSize", 500)
                .containsEntry("readModelFallbackEnabled", true)
                .containsEntry("authoritative", false)
                .containsEntry("writeCutoverAllowed", false)
                .containsEntry("mode", "read_only_shadow_foundation")
                .containsEntry("rollbackStrategy", "disable_people.shadow.local-persistence.enabled")
                .containsEntry("backfillRecordsTotal", 3.0d)
                .containsEntry("backfillTablesPlannedTotal", 0.0d)
                .containsEntry("reconciliationDivergencesTotal", 1.0d)
                .containsEntry("reconciliationTablesPlannedTotal", 0.0d)
                .containsEntry("operationCyclesTotal", 0.0d)
                .containsEntry("readRoutingDecisionsTotal", 0.0d)
                .containsEntry("localCatalogReadsTotal", 0.0d)
                .containsEntry("localIdentityReadsTotal", 0.0d)
                .containsEntry("localStudentResponsibleReadsTotal", 0.0d)
                .containsEntry("failuresTotal", 0.0d);

        @SuppressWarnings("unchecked")
        Map<String, Object> shadowReadRoutes = (Map<String, Object>) health.getDetails().get("shadowReadRoutes");
        @SuppressWarnings("unchecked")
        Map<String, Object> buscarPorId = (Map<String, Object>) shadowReadRoutes.get("buscarPorId");
        assertThat(buscarPorId)
                .containsEntry("shadowRoute", "GET /internal/v1/pessoas/{id}")
                .containsEntry("candidateSource", "pessoa")
                .containsEntry("currentSource", "monolith_proxy")
                .containsEntry("localReadEnabled", false)
                .containsEntry("fallbackRequired", true);

        @SuppressWarnings("unchecked")
        Map<String, Object> readRoutingPlan = (Map<String, Object>) health.getDetails().get("readRoutingPlan");
        @SuppressWarnings("unchecked")
        Map<String, Object> buscarPorIdRouting = (Map<String, Object>) readRoutingPlan.get("buscarPorId");
        assertThat(buscarPorIdRouting)
                .containsEntry("selectedSource", "monolith_proxy")
                .containsEntry("localReadRequested", false)
                .containsEntry("localReadEligible", false)
                .containsEntry("fallbackEnabled", true)
                .containsEntry("writesEnabled", false)
                .containsEntry("reason", "read-model-cutover-disabled");

        @SuppressWarnings("unchecked")
        java.util.List<String> readModelTables = (java.util.List<String>) health.getDetails().get("readModelTables");
        assertThat(readModelTables)
                .containsExactly(
                        "tipo_pessoa",
                        "tipo_endereco",
                        "pessoa",
                        "pessoa_tipo_pessoa",
                        "aluno",
                        "responsavel",
                        "aluno_responsavel");

        @SuppressWarnings("unchecked")
        Map<String, Object> backfillPlan = (Map<String, Object>) health.getDetails().get("backfillPlan");
        @SuppressWarnings("unchecked")
        Map<String, Object> pessoaPlan = (Map<String, Object>) backfillPlan.get("pessoa");
        assertThat(pessoaPlan)
                .containsEntry("source", "monolith_proxy")
                .containsEntry("target", "people_read_model_candidate")
                .containsEntry("backfillEnabled", false)
                .containsEntry("reconciliationEnabled", false)
                .containsEntry("writesEnabled", false)
                .containsEntry("cutoverEnabled", false)
                .containsEntry("idempotent", true);

        @SuppressWarnings("unchecked")
        Map<String, Object> catalogSchemaPlan =
                (Map<String, Object>) health.getDetails().get("catalogReadModelSchemaPlan");
        assertThat(catalogSchemaPlan)
                .containsEntry("status", "local_catalog_read_adapter_prepared")
                .containsEntry("recommendedNextStep",
                        "close_phase_63_and_plan_next_people_service_scope")
                .containsEntry("migrationAllowedNow", true)
                .containsEntry("physicalSchemaRequiredNext", true)
                .containsEntry("localReadAdapterRequiredNext", true)
                .containsEntry("readCutoverAllowed", false)
                .containsEntry("writeCutoverAllowed", false);

        Object schemaMigration = health.getDetails().get("schemaMigration");
        assertThat(schemaMigration).isNotNull();
        Object catalogBackfill = health.getDetails().get("catalogBackfill");
        assertThat(catalogBackfill).isNotNull();
        Object localReadModelBackfill = health.getDetails().get("localReadModelBackfill");
        assertThat(localReadModelBackfill).isNotNull();
        @SuppressWarnings("unchecked")
        Map<String, Object> transactionalPlan =
                (Map<String, Object>) health.getDetails().get("transactionalReadModelExpansionPlan");
        assertThat(transactionalPlan)
                .containsEntry("status", "address_schema_backfill_diagnostic_closed_no_migration")
                .containsEntry("recommendedNextStep",
                        "prepare_address_schema_migration_opt_in_without_backfill_or_cutover")
                .containsEntry("minimalNextSlice", "address_schema_migration_opt_in_no_backfill")
                .containsEntry("migrationAllowedNow", false)
                .containsEntry("backfillAllowedNow", false)
                .containsEntry("localReadCutoverAllowedNow", false);

        @SuppressWarnings("unchecked")
        Map<String, Object> nextBlockedSlice =
                (Map<String, Object>) health.getDetails().get("nextBlockedSliceDiagnostic");
        assertThat(nextBlockedSlice)
                .containsEntry("slice", "endereco")
                .containsEntry("status", "schema_backfill_diagnostic_closed_migration_still_blocked")
                .containsEntry("implementationAllowedNow", false)
                .containsEntry("schemaAllowedNow", false)
                .containsEntry("backfillAllowedNow", false)
                .containsEntry("localReadCutoverAllowedNow", false)
                .containsEntry("dependsOnClosedSlice", "consultarCadastro")
                .containsEntry("firstSafeImplementationSlice", "address_schema_migration_opt_in_no_backfill");
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> preparedInternalContract =
                (java.util.Map<String, Object>) nextBlockedSlice.get("preparedInternalContract");
        assertThat(preparedInternalContract)
                .containsEntry("port", "PessoaEnderecoPort")
                .containsEntry("summary", "PessoaEnderecoResumo")
                .containsEntry("jpaEntityExposure", false);
        @SuppressWarnings("unchecked")
        java.util.List<String> requiredContractDecisions =
                (java.util.List<String>) nextBlockedSlice.get("requiredContractDecisions");
        assertThat(requiredContractDecisions).contains(
                "separate-external-cep-lookup-from-persisted-address-data",
                "define-reconciliation-key-by-pessoa_endereco-before-backfill");
        @SuppressWarnings("unchecked")
        java.util.List<String> writeConsumers = (java.util.List<String>) nextBlockedSlice.get("writeConsumers");
        assertThat(writeConsumers).contains(
                "PessoaFoundationService.criarPessoaComTipoEEndereco",
                "CriarAlunoUseCase",
                "AtualizarResponsavelUseCase");
        @SuppressWarnings("unchecked")
        java.util.List<String> cleanupConsumers = (java.util.List<String>) nextBlockedSlice.get("cleanupConsumers");
        assertThat(cleanupConsumers).contains(
                "PessoaEnderecoJpaRepository.deleteByPessoaId",
                "PessoaEnderecoJpaRepository.countByEnderecoId");
        @SuppressWarnings("unchecked")
        java.util.List<String> cepLookupConsumers = (java.util.List<String>) nextBlockedSlice.get("cepLookupConsumers");
        assertThat(cepLookupConsumers).contains(
                "EnderecoCepController.GET /enderecos/cep/{cep}",
                "ViaCepService.consultar");
        @SuppressWarnings("unchecked")
        java.util.List<String> greenCriteriaBeforeSchema =
                (java.util.List<String>) nextBlockedSlice.get("greenCriteriaBeforeSchema");
        assertThat(greenCriteriaBeforeSchema).contains(
                "internal-address-contract-defined-without-jpa-entities",
                "orphan-address-cleanup-strategy-defined",
                "cep-lookup-kept-as-external-adapter");

        @SuppressWarnings("unchecked")
        Map<String, Object> addressDiagnostic =
                (Map<String, Object>) health.getDetails().get("addressSchemaBackfillDiagnostic");
        assertThat(addressDiagnostic)
                .containsEntry("slice", "endereco_pessoa_endereco")
                .containsEntry("status", "schema_backfill_diagnostic_closed_migration_still_blocked")
                .containsEntry("migrationAllowedNow", false)
                .containsEntry("backfillAllowedNow", false)
                .containsEntry("localReadCutoverAllowedNow", false)
                .containsEntry("reconciliationKey", "pessoa_endereco.id_pessoa_endereco")
                .containsEntry("nextImplementationSlice", "address_schema_migration_opt_in_no_backfill");
        @SuppressWarnings("unchecked")
        Map<String, java.util.List<String>> minimalColumns =
                (Map<String, java.util.List<String>>) addressDiagnostic.get("minimalColumns");
        assertThat(minimalColumns.get("endereco")).containsExactly(
                "id_endereco",
                "cep",
                "logradouro",
                "numero",
                "complemento",
                "bairro",
                "cidade",
                "uf",
                "created_at",
                "updated_at");
        assertThat(minimalColumns.get("pessoa_endereco")).containsExactly(
                "id_pessoa_endereco",
                "id_pessoa",
                "id_endereco",
                "id_tipo_endereco",
                "principal",
                "created_at");
        assertThat((String) addressDiagnostic.get("principalAddressRule"))
                .contains("multiple principal records block green reconciliation");
        assertThat((String) addressDiagnostic.get("cepLookupPolicy"))
                .contains("ViaCEP remains an external lookup adapter");
        @SuppressWarnings("unchecked")
        java.util.List<String> secondaryChecks =
                (java.util.List<String>) addressDiagnostic.get("secondaryReconciliationChecks");
        assertThat(secondaryChecks).contains(
                "id_pessoa",
                "id_endereco",
                "id_tipo_endereco",
                "principal",
                "normalized_cep_logradouro_numero_bairro_cidade_uf");

        @SuppressWarnings("unchecked")
        Map<String, Object> closure =
                (Map<String, Object>) health.getDetails().get("guardedReadCutoverClosure");
        assertThat(closure)
                .containsEntry("operation", "consultarCadastro")
                .containsEntry("status", "guarded_local_read_blocked")
                .containsEntry("selectedSource", "monolith_proxy")
                .containsEntry("localCandidateSource", "people_read_model_student_responsible")
                .containsEntry("fallbackSource", "monolith_proxy")
                .containsEntry("fallbackRequired", true)
                .containsEntry("nextSliceBlocked", "endereco")
                .containsEntry("reason", "read-model-cutover-disabled");
        @SuppressWarnings("unchecked")
        java.util.List<String> rollbackSteps = (java.util.List<String>) closure.get("rollbackSteps");
        assertThat(rollbackSteps).contains(
                "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                "keep-consultarCadastro-on-monolith-proxy-when-guard-is-not-green");
    }

    @Test
    void deveReportarOutOfServiceQuandoPersistenciaLocalForLigadaAntesDoSchemaEBackfill() {
        PeopleLocalPersistenceHealthIndicator indicator = new PeopleLocalPersistenceHealthIndicator(
                new PeopleLocalPersistenceProperties(true, false, false, false, false, false, 500, true),
                new SimpleMeterRegistry(),
                new br.com.escola.peopleservice.application.service.PeopleLocalReadCutoverGuard(
                        new PeopleLocalPersistenceProperties(true, false, false, false, false, false, 500, true),
                        new SimpleMeterRegistry(),
                        new PeopleLocalPersistenceOperationState()),
                new br.com.escola.peopleservice.application.service.PeopleCatalogReadModelSchemaPlanner(),
                new br.com.escola.peopleservice.application.service.PeopleTransactionalReadModelExpansionPlanner(),
                new PeopleLocalReadModelSchemaMigrationState(),
                new PeopleLocalPersistenceOperationState());

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
        assertThat(health.getDetails())
                .containsEntry("enabled", true)
                .containsEntry("reason", "local-persistence-foundation-only")
                .containsEntry("writeCutoverAllowed", false);
    }

    @Test
    void deveReportarOutOfServiceQuandoCutoverLocalForLigadoAntesDaImplementacao() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleLocalPersistenceHealthIndicator indicator = new PeopleLocalPersistenceHealthIndicator(
                new PeopleLocalPersistenceProperties(true, true, true, true, true, true, 100, true),
                meterRegistry,
                new br.com.escola.peopleservice.application.service.PeopleLocalReadCutoverGuard(
                        new PeopleLocalPersistenceProperties(true, true, true, true, true, true, 100, true),
                        meterRegistry,
                        new PeopleLocalPersistenceOperationState()),
                new br.com.escola.peopleservice.application.service.PeopleCatalogReadModelSchemaPlanner(),
                new br.com.escola.peopleservice.application.service.PeopleTransactionalReadModelExpansionPlanner(),
                new PeopleLocalReadModelSchemaMigrationState(),
                new PeopleLocalPersistenceOperationState());

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
        assertThat(health.getDetails())
                .containsEntry("readModelCutoverEnabled", true)
                .containsEntry("reason", "local-read-model-backfill-not-green")
                .containsEntry("authoritative", false);
    }

    @Test
    void deveReportarUpQuandoCutoverLocalEstaHabilitadoComBackfillVerde() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleLocalPersistenceOperationState operationState = new PeopleLocalPersistenceOperationState();
        operationState.update(new br.com.escola.peopleservice.application.dto.PeopleLocalPersistenceOperationReport(
                true,
                true,
                "completed",
                "local-read-model-backfill-and-reconciliation-completed",
                100,
                7,
                7,
                3,
                3,
                3,
                0,
                false,
                false,
                java.util.List.of()));
        var properties = new PeopleLocalPersistenceProperties(true, true, true, true, true, true, 100, true);
        PeopleLocalPersistenceHealthIndicator indicator = new PeopleLocalPersistenceHealthIndicator(
                properties,
                meterRegistry,
                new br.com.escola.peopleservice.application.service.PeopleLocalReadCutoverGuard(
                        properties,
                        meterRegistry,
                        operationState),
                new br.com.escola.peopleservice.application.service.PeopleCatalogReadModelSchemaPlanner(),
                new br.com.escola.peopleservice.application.service.PeopleTransactionalReadModelExpansionPlanner(),
                new PeopleLocalReadModelSchemaMigrationState(),
                operationState);

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("readModelCutoverEnabled", true)
                .containsEntry("reason", "read-model-cutover-eligible");
        @SuppressWarnings("unchecked")
        Map<String, Object> shadowReadRoutes = (Map<String, Object>) health.getDetails().get("shadowReadRoutes");
        @SuppressWarnings("unchecked")
        Map<String, Object> consulta = (Map<String, Object>) shadowReadRoutes.get("consultarCadastro");
        assertThat(consulta)
                .containsEntry("currentSource", "people_read_model_student_responsible")
                .containsEntry("localReadEnabled", true)
                .containsEntry("fallbackRequired", true)
                .containsEntry("reason", "local-student-responsible-read-eligible");
        @SuppressWarnings("unchecked")
        Map<String, Object> closure =
                (Map<String, Object>) health.getDetails().get("guardedReadCutoverClosure");
        assertThat(closure)
                .containsEntry("operation", "consultarCadastro")
                .containsEntry("status", "guarded_local_read_enabled")
                .containsEntry("selectedSource", "people_read_model_student_responsible")
                .containsEntry("fallbackRequired", true)
                .containsEntry("fallbackEnabled", true)
                .containsEntry("writesEnabled", false)
                .containsEntry("nextSliceBlocked", "endereco");
        @SuppressWarnings("unchecked")
        java.util.Map<String, String> metrics = (java.util.Map<String, String>) closure.get("metrics");
        assertThat(metrics)
                .containsEntry("localReads", "people.shadow.local.persistence.student.responsible.reads")
                .containsEntry("routingDecisions", "people.shadow.local.persistence.read.routing.decisions");
    }
}
