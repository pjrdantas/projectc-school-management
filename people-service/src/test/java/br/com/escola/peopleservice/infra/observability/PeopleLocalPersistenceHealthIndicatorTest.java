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
                new br.com.escola.peopleservice.application.service.PeopleAddressWriteAuthorityPlanner(),
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
                .containsEntry("addressReadRoutingDecisionsTotal", 0.0d)
                .containsEntry("localAddressReadsTotal", 0.0d)
                .containsEntry("localDocumentMetadataReadsTotal", 0.0d)
                .containsEntry("localContactReadsTotal", 0.0d)
                .containsEntry("addressWriteShadowCommandsTotal", 0.0d)
                .containsEntry("monolithAddressWriteRequestsTotal", 0.0d)
                .containsEntry("monolithAddressWriteFailuresTotal", 0.0d)
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
                        "aluno_responsavel",
                        "endereco",
                        "pessoa_endereco",
                        "people_documento_read_model",
                        "people_funcionario_read_model");

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
                .containsEntry("status", "address_local_read_internal_guard_connection_prepared_no_route")
                .containsEntry("recommendedNextStep",
                        "close_phase_70_and_plan_address_write_authority_diagnostic")
                .containsEntry("minimalNextSlice", "address_adapter_connected_internal_guard_no_external_route")
                .containsEntry("migrationAllowedNow", true)
                .containsEntry("backfillAllowedNow", true)
                .containsEntry("localReadCutoverAllowedNow", false);

        @SuppressWarnings("unchecked")
        Map<String, Object> nextBlockedSlice =
                (Map<String, Object>) health.getDetails().get("nextBlockedSliceDiagnostic");
        assertThat(nextBlockedSlice)
                .containsEntry("slice", "endereco")
                .containsEntry("status", "address_local_read_internal_guard_connection_prepared_no_route")
                .containsEntry("implementationAllowedNow", true)
                .containsEntry("schemaAllowedNow", true)
                .containsEntry("backfillAllowedNow", true)
                .containsEntry("localReadCutoverAllowedNow", false)
                .containsEntry("dependsOnClosedSlice", "consultarCadastro")
                .containsEntry("firstSafeImplementationSlice",
                        "address_adapter_connected_internal_guard_no_external_route");
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> preparedInternalContract =
                (java.util.Map<String, Object>) nextBlockedSlice.get("preparedInternalContract");
        assertThat(preparedInternalContract)
                .containsEntry("port", "PeopleAddressLocalReadPort")
                .containsEntry("response", "PessoaEnderecoLocalReadResponse")
                .containsEntry("adapter", "JdbcPeopleAddressLocalReadAdapter")
                .containsEntry("internalService", "PeopleAddressLocalReadService")
                .containsEntry("routingOperation", "addressLocalRead")
                .containsEntry("jpaEntityExposure", false);
        @SuppressWarnings("unchecked")
        java.util.List<String> requiredContractDecisions =
                (java.util.List<String>) nextBlockedSlice.get("requiredContractDecisions");
        assertThat(requiredContractDecisions).contains(
                "define-internal-address-read-payload-before-adapter",
                "separate-external-cep-lookup-from-persisted-address-data",
                "keep-read-cutover-blocked-until-address-reconciliation-is-green");
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
                .containsEntry("status", "backfill_reconciliation_prepared_read_cutover_still_blocked")
                .containsEntry("migrationAllowedNow", true)
                .containsEntry("backfillAllowedNow", true)
                .containsEntry("localReadCutoverAllowedNow", false)
                .containsEntry("reconciliationKey", "pessoa_endereco.id_pessoa_endereco")
                .containsEntry("nextImplementationSlice", "phase_68_closure_no_cutover");
        @SuppressWarnings("unchecked")
        Map<String, Object> addressSchemaMigration = (Map<String, Object>) addressDiagnostic.get("schemaMigration");
        assertThat(addressSchemaMigration)
                .containsEntry("version", "V4__create_people_address_read_model.sql")
                .containsEntry("enabledByDefault", false)
                .containsEntry("automaticBackfill", false);
        @SuppressWarnings("unchecked")
        Map<String, Object> backfillReconciliation =
                (Map<String, Object>) addressDiagnostic.get("backfillReconciliation");
        assertThat(backfillReconciliation)
                .containsEntry("enabledByDefault", false)
                .containsEntry("source", "monolith_jdbc")
                .containsEntry("target", "people_read_model_address");
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
        Map<String, Object> addressLocalReadContract =
                (Map<String, Object>) health.getDetails().get("addressLocalReadContractDiagnostic");
        assertThat(addressLocalReadContract)
                .containsEntry("slice", "endereco_local_read_contract")
                .containsEntry("status", "adapter_connected_to_internal_guard_no_route")
                .containsEntry("phase", "Fase 69")
                .containsEntry("contractAllowedNow", true)
                .containsEntry("localReadAdapterAllowedNow", false)
                .containsEntry("localReadAdapterPrepared", true)
                .containsEntry("localReadAdapterConnected", true)
                .containsEntry("localReadCutoverAllowedNow", false)
                .containsEntry("externalRouteChangeAllowedNow", false)
                .containsEntry("bffFrontendChangeAllowedNow", false)
                .containsEntry("writeCutoverAllowedNow", false)
                .containsEntry("candidateSource", "people_read_model_address")
                .containsEntry("fallbackSource", "monolith_proxy")
                .containsEntry("fallbackRequired", true)
                .containsEntry("nextImplementationSlice",
                        "address_adapter_connected_internal_guard_no_external_route");
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> preparedArtifacts =
                (java.util.Map<String, Object>) addressLocalReadContract.get("preparedArtifacts");
        assertThat(preparedArtifacts)
                .containsEntry("port", "PeopleAddressLocalReadPort")
                .containsEntry("response", "PessoaEnderecoLocalReadResponse")
                .containsEntry("adapter", "JdbcPeopleAddressLocalReadAdapter")
                .containsEntry("internalService", "PeopleAddressLocalReadService")
                .containsEntry("routeCreated", false)
                .containsEntry("adapterCreated", true)
                .containsEntry("queryServiceConnected", false);
        @SuppressWarnings("unchecked")
        java.util.List<String> minimalInternalPayload =
                (java.util.List<String>) addressLocalReadContract.get("minimalInternalPayload");
        assertThat(minimalInternalPayload).contains(
                "id_pessoa_endereco",
                "id_pessoa",
                "id_endereco",
                "id_tipo_endereco",
                "tipo_endereco_codigo",
                "principal",
                "cep",
                "logradouro",
                "cidade",
                "uf");
        @SuppressWarnings("unchecked")
        java.util.List<String> guardPreconditions =
                (java.util.List<String>) addressLocalReadContract.get("guardPreconditions");
        assertThat(guardPreconditions).contains(
                "people.shadow.local-persistence.read-model-fallback-enabled=true",
                "address-reconciliation-has-no-multiple-principal-addresses",
                "address-reconciliation-has-no-normalized-field-divergence");
        @SuppressWarnings("unchecked")
        java.util.List<String> outOfScope = (java.util.List<String>) addressLocalReadContract.get("outOfScope");
        assertThat(outOfScope).contains(
                "new-internal-rest-route",
                "bff-route-change",
                "frontend-change",
                "consultarCadastro-payload-change",
                "address-write-cutover");

        @SuppressWarnings("unchecked")
        Map<String, Object> addressCutoverEligibility =
                (Map<String, Object>) health.getDetails().get("addressLocalReadCutoverEligibilityDiagnostic");
        assertThat(addressCutoverEligibility)
                .containsEntry("slice", "endereco_read_cutover_eligibility")
                .containsEntry("phase", "Fase 70")
                .containsEntry("status", "adapter_connected_to_internal_guard_no_route")
                .containsEntry("routingOperation", "addressLocalRead")
                .containsEntry("shadowRoute", "internal-operation:PeopleAddressLocalReadPort")
                .containsEntry("selectedSource", "monolith_proxy")
                .containsEntry("localReadEligible", false)
                .containsEntry("reason", "read-model-cutover-disabled")
                .containsEntry("localReadCutoverAllowedNow", false)
                .containsEntry("adapterPrepared", true)
                .containsEntry("internalGuardedServiceConnected", true)
                .containsEntry("queryServiceConnected", false)
                .containsEntry("routeCreated", false)
                .containsEntry("bffFrontendChangeAllowedNow", false)
                .containsEntry("writeCutoverAllowedNow", false)
                .containsEntry("candidateSource", "people_read_model_address")
                .containsEntry("fallbackSource", "monolith_proxy")
                .containsEntry("fallbackRequired", true)
                .containsEntry("recommendedNextStep",
                        "close_phase_70_and_plan_address_write_authority_diagnostic");
        @SuppressWarnings("unchecked")
        java.util.List<String> minimumGuardCriteria =
                (java.util.List<String>) addressCutoverEligibility.get("minimumGuardCriteria");
        assertThat(minimumGuardCriteria).contains(
                "people.shadow.local-persistence.read-model-fallback-enabled=true",
                "localReadModelBackfill.status=completed",
                "address-reconciliation-has-no-multiple-principal-addresses");
        @SuppressWarnings("unchecked")
        java.util.List<String> blockersBeforeExternalExposure =
                (java.util.List<String>) addressCutoverEligibility.get("blockersBeforeExternalExposure");
        assertThat(blockersBeforeExternalExposure).contains(
                "consultarCadastro-current-payload-does-not-expose-address");
        assertThat(blockersBeforeExternalExposure)
                .doesNotContain("no-address-specific-read-routing-operation-yet")
                .doesNotContain("no-address-specific-observability-metric-yet");
        @SuppressWarnings("unchecked")
        java.util.Map<String, String> addressMetrics =
                (java.util.Map<String, String>) addressCutoverEligibility.get("metrics");
        assertThat(addressMetrics)
                .containsEntry("addressRoutingDecisions",
                        "people.shadow.local.persistence.address.read.routing.decisions")
                .containsEntry("localReads", "people.shadow.local.persistence.address.reads");
        @SuppressWarnings("unchecked")
        java.util.List<String> eligibilityOutOfScope =
                (java.util.List<String>) addressCutoverEligibility.get("explicitlyOutOfScope");
        assertThat(eligibilityOutOfScope).contains(
                "create-new-address-rest-route",
                "bff-route-change",
                "frontend-change",
                "address-write-cutover");

        @SuppressWarnings("unchecked")
        Map<String, Object> addressWriteAuthority =
                (Map<String, Object>) health.getDetails().get("addressWriteAuthorityDiagnostic");
        assertThat(addressWriteAuthority)
                .containsEntry("phase", "Fase 71")
                .containsEntry("slice", "endereco_write_authority")
                .containsEntry("status", "backend_shadow_command_service_prepared_no_write_cutover")
                .containsEntry("recommendedNextStep", "close_phase_71_or_plan_monolith_write_adapter_diagnostic")
                .containsEntry("minimalNextSlice",
                        "address_write_shadow_command_service_no_local_persistence")
                .containsEntry("writeCutoverAllowedNow", false)
                .containsEntry("migrationAllowedNow", false)
                .containsEntry("backfillAllowedNow", false)
                .containsEntry("localReadPrerequisiteClosed", true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> preparedCommandArtifacts =
                (java.util.Map<String, Object>) addressWriteAuthority.get("preparedCommandArtifacts");
        assertThat(preparedCommandArtifacts)
                .containsEntry("port", "PeopleAddressWritePort")
                .containsEntry("writeCommand", "PessoaEnderecoWriteCommand")
                .containsEntry("cleanupCommand", "PessoaEnderecoCleanupCommand")
                .containsEntry("result", "PessoaEnderecoWriteResult")
                .containsEntry("shadowService", "PeopleAddressWriteFallbackService")
                .containsEntry("adapterCreated", true)
                .containsEntry("routeCreated", false)
                .containsEntry("localPersistenceConnected", false);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> shadowCommandExecution =
                (java.util.Map<String, Object>) addressWriteAuthority.get("shadowCommandExecution");
        assertThat(shadowCommandExecution)
                .containsEntry("service", "PeopleAddressWriteFallbackService")
                .containsEntry("metric", "people.shadow.local.persistence.address.write.shadow.commands")
                .containsEntry("selectedSource", "monolith_proxy")
                .containsEntry("persistedLocally", false)
                .containsEntry("fallbackRequired", true)
                .containsEntry("localWriteEnabled", false);
        @SuppressWarnings("unchecked")
        java.util.List<Object> candidateOperations =
                (java.util.List<Object>) addressWriteAuthority.get("candidateOperations");
        assertThat(candidateOperations).hasSize(4);
        @SuppressWarnings("unchecked")
        java.util.List<String> monolithWriteAuthorities =
                (java.util.List<String>) addressWriteAuthority.get("monolithWriteAuthorities");
        assertThat(monolithWriteAuthorities).contains(
                "PessoaFoundationService.criarPessoaComTipoEEndereco",
                "PessoaFoundationService.atualizarPessoaEEndereco",
                "PessoaEnderecoPort.removerEnderecosDaPessoaRemovendoOrfaos",
                "ViaCepService");
        @SuppressWarnings("unchecked")
        java.util.List<String> requiredContracts =
                (java.util.List<String>) addressWriteAuthority.get("requiredContracts");
        assertThat(requiredContracts).contains(
                "PeopleAddressWritePort command payload without JPA entities defined",
                "PessoaEnderecoWriteCommand carries idempotency key for write attempts",
                "PessoaEnderecoCleanupCommand carries orphan cleanup intent",
                "PessoaEnderecoWriteResult exposes selected source, local persistence flag and fallback requirement",
                "PeopleAddressWriteFallbackService records fallback decisions without writing local tables");
        @SuppressWarnings("unchecked")
        java.util.List<String> writeOutOfScope =
                (java.util.List<String>) addressWriteAuthority.get("explicitlyOutOfScope");
        assertThat(writeOutOfScope).contains(
                "create-address-write-rest-route",
                "bff-route-change",
                "frontend-change",
                "write-to-local-address-tables");

        @SuppressWarnings("unchecked")
        Map<String, Object> addressWriteMonolithAdapter =
                (Map<String, Object>) health.getDetails().get("addressWriteMonolithAdapterDiagnostic");
        assertThat(addressWriteMonolithAdapter)
                .containsEntry("phase", "Fase 72")
                .containsEntry("slice", "address_write_monolith_adapter_diagnostic")
                .containsEntry("status", "monolith_write_adapter_prepared_guard_disabled_no_cutover")
                .containsEntry("recommendedNextStep",
                        "close_phase_72_and_plan_next_people_backend_scope")
                .containsEntry("minimalNextSlice", "phase_72_closure_no_write_cutover")
                .containsEntry("monolithHttpWriteContractAvailable", true)
                .containsEntry("adapterImplementationAllowedNow", true)
                .containsEntry("writeCutoverAllowedNow", false)
                .containsEntry("localPersistenceAllowedNow", false);
        @SuppressWarnings("unchecked")
        java.util.List<Object> adapterCandidateOperations =
                (java.util.List<Object>) addressWriteMonolithAdapter.get("candidateOperations");
        assertThat(adapterCandidateOperations).hasSize(2);
        @SuppressWarnings("unchecked")
        java.util.List<String> requiredMonolithContracts =
                (java.util.List<String>) addressWriteMonolithAdapter.get("requiredMonolithContracts");
        assertThat(requiredMonolithContracts).contains(
                "PUT /internal/pessoas/{pessoaId}/endereco-principal",
                "DELETE /internal/pessoas/{pessoaId}/enderecos",
                "Idempotency-Key header required");
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> currentPeopleServiceState =
                (java.util.Map<String, Object>) addressWriteMonolithAdapter.get("currentPeopleServiceState");
        assertThat(currentPeopleServiceState)
                .containsEntry("shadowService", "PeopleAddressWriteFallbackService")
                .containsEntry("writePort", "PeopleAddressWritePort")
                .containsEntry("monolithWriteClientCreated", true)
                .containsEntry("monolithWriteClient", "MonolithPessoaAddressWriteClient")
                .containsEntry("monolithWriteClientEnabledByDefault", false)
                .containsEntry("guardProperty", "people.shadow.monolith.address-write-adapter-enabled")
                .containsEntry("localPersistenceConnected", false)
                .containsEntry("routeCreated", false);
        @SuppressWarnings("unchecked")
        java.util.List<String> adapterOutOfScope =
                (java.util.List<String>) addressWriteMonolithAdapter.get("explicitlyOutOfScope");
        assertThat(adapterOutOfScope).contains(
                "activate-address-write-cutover",
                "bff-route-change",
                "frontend-change",
                "local-address-write-persistence");

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
                new br.com.escola.peopleservice.application.service.PeopleAddressWriteAuthorityPlanner(),
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
                new br.com.escola.peopleservice.application.service.PeopleAddressWriteAuthorityPlanner(),
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
                new br.com.escola.peopleservice.application.service.PeopleAddressWriteAuthorityPlanner(),
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

        @SuppressWarnings("unchecked")
        Map<String, Object> addressScopeClosure =
                (Map<String, Object>) health.getDetails().get("peopleAddressScopeClosureDiagnostic");
        assertThat(addressScopeClosure)
                .containsEntry("phase", "Fase 73")
                .containsEntry("slice", "people_address_scope_closure_review")
                .containsEntry("status", "people_address_scope_review_closed_ready_for_next_family_diagnostic")
                .containsEntry("recommendedNextStep",
                        "start_people_document_scope_diagnostic_without_reopening_address_cutover")
                .containsEntry("minimalNextSlice", "people_document_contract_diagnostic")
                .containsEntry("readScopeClosed", true)
                .containsEntry("writeScopePreparedWithoutCutover", true)
                .containsEntry("activationRequiredNow", false)
                .containsEntry("safeToStartNextFamilyDiagnostic", true);
        @SuppressWarnings("unchecked")
        java.util.List<String> remainingActivationBlockers =
                (java.util.List<String>) addressScopeClosure.get("remainingActivationBlockers");
        assertThat(remainingActivationBlockers).contains(
                "create-person-with-address remains inside monolith person transaction",
                "funcionario, professor and pessoa_documento are still outside current people-service scope");
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> currentRecommendation =
                (java.util.Map<String, Object>) addressScopeClosure.get("currentRecommendation");
        assertThat(currentRecommendation)
                .containsEntry("keepAddressGuardDisabled", true)
                .containsEntry("keepAddressWritesOnMonolith", true)
                .containsEntry("nextPreferredFamily", "pessoa_documento")
                .containsEntry("reopenAddressInThisPhase", false);

        @SuppressWarnings("unchecked")
        Map<String, Object> peopleDocumentScope =
                (Map<String, Object>) health.getDetails().get("peopleDocumentScopeDiagnostic");
        assertThat(peopleDocumentScope)
                .containsEntry("phase", "Fase 73")
                .containsEntry("slice", "people_document_contract_diagnostic")
                .containsEntry("status", "document_metadata_read_contract_preferred_write_cleanup_stays_on_monolith")
                .containsEntry("recommendedNextStep",
                        "prepare_internal_people_document_metadata_read_contract_without_bff_or_write_cutover")
                .containsEntry("minimalNextSlice", "people_document_internal_metadata_read_contract")
                .containsEntry("diagnosticReadyNow", true)
                .containsEntry("internalContractSeparationAllowedNow", true)
                .containsEntry("localPersistenceAllowedNow", false)
                .containsEntry("externalRouteChangeAllowedNow", false)
                .containsEntry("fallbackToCurrentMonolithRequired", true);
        @SuppressWarnings("unchecked")
        java.util.List<String> minimalReadCandidates =
                (java.util.List<String>) peopleDocumentScope.get("minimalReadCandidates");
        assertThat(minimalReadCandidates).contains(
                "listar metadados de documentos por pessoa para aluno/responsavel sem mover upload");
        @SuppressWarnings("unchecked")
        java.util.List<String> monolithDependencies =
                (java.util.List<String>) peopleDocumentScope.get("monolithDependencies");
        assertThat(monolithDependencies).contains(
                "DocumentoPersistenceGateway.findByEntidade/findById/deleteByEntidade",
                "AlunoPersistenceGateway.deleteById e ResponsavelPersistenceGateway.deleteById");
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> peopleDocumentRecommendation =
                (java.util.Map<String, Object>) peopleDocumentScope.get("currentRecommendation");
        assertThat(peopleDocumentRecommendation)
                .containsEntry("preferFirstImplementation", "internal_metadata_read_only")
                .containsEntry("keepWritesOnMonolith", true)
                .containsEntry("keepCleanupOnMonolith", true)
                .containsEntry("prepareExternalRouteNow", false);

        @SuppressWarnings("unchecked")
        Map<String, Object> peopleDocumentInternalContract =
                (Map<String, Object>) health.getDetails().get("peopleDocumentInternalMetadataReadContractDiagnostic");
        assertThat(peopleDocumentInternalContract)
                .containsEntry("phase", "Fase 73")
                .containsEntry("slice", "people_document_internal_metadata_read_contract")
                .containsEntry("status", "internal_contract_prepared_no_adapter_no_route")
                .containsEntry("recommendedNextStep",
                        "close_phase_73_and_plan_people_document_local_adapter_diagnostic")
                .containsEntry("minimalNextSlice", "people_document_local_metadata_adapter_diagnostic")
                .containsEntry("contractPrepared", true)
                .containsEntry("internalServicePrepared", true)
                .containsEntry("adapterCreated", false)
                .containsEntry("localPersistenceConnected", false)
                .containsEntry("externalRouteCreated", false)
                .containsEntry("fallbackRequired", true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> preparedDocumentArtifacts =
                (java.util.Map<String, Object>) peopleDocumentInternalContract.get("preparedArtifacts");
        assertThat(preparedDocumentArtifacts)
                .containsEntry("port", "PeopleDocumentMetadataLocalReadPort")
                .containsEntry("response", "PessoaDocumentoMetadataLocalReadResponse")
                .containsEntry("internalService", "PeopleDocumentMetadataLocalReadService")
                .containsEntry("adapterCreated", false)
                .containsEntry("routeCreated", false)
                .containsEntry("localPersistenceConnected", false);

        @SuppressWarnings("unchecked")
        Map<String, Object> peopleDocumentLocalCandidate =
                (Map<String, Object>) health.getDetails().get("peopleDocumentLocalReadCandidateDiagnostic");
        assertThat(peopleDocumentLocalCandidate)
                .containsEntry("phase", "Fase 74")
                .containsEntry("slice", "people_document_local_metadata_adapter_diagnostic")
                .containsEntry("status",
                        "metadata_local_read_candidate_diagnostic_started_continue_document_family")
                .containsEntry("recommendedNextStep",
                        "prepare_people_document_local_metadata_schema_diagnostic_without_route_or_upload_migration")
                .containsEntry("minimalNextSlice", "people_document_local_metadata_schema_diagnostic")
                .containsEntry("schemaDiagnosticAllowedNow", true)
                .containsEntry("adapterDiagnosticAllowedNow", true)
                .containsEntry("continueWithDocumentFamilyNow", true)
                .containsEntry("switchToFuncionarioNow", false)
                .containsEntry("localReadCutoverAllowedNow", false)
                .containsEntry("reconciliationKey", "pessoa_documento.id_pessoa_documento");
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> documentDecision =
                (java.util.Map<String, Object>) peopleDocumentLocalCandidate.get("currentDecision");
        assertThat(documentDecision)
                .containsEntry("preferredNextFamily", "pessoa_documento")
                .containsEntry("continueWithSchemaDiagnostic", true)
                .containsEntry("switchToFuncionarioAfterThisDiagnostic", false)
                .containsEntry("prepareRouteNow", false);

        @SuppressWarnings("unchecked")
        Map<String, Object> peopleDocumentSchema =
                (Map<String, Object>) health.getDetails().get("peopleDocumentMetadataSchemaDiagnostic");
        assertThat(peopleDocumentSchema)
                .containsEntry("phase", "Fase 75")
                .containsEntry("slice", "people_document_local_metadata_schema_diagnostic")
                .containsEntry("status", "schema_preserved_and_jdbc_adapter_prepared_still_not_activated")
                .containsEntry("recommendedNextStep",
                        "close_phase_75_and_plan_people_document_backfill_reconciliation_preparation")
                .containsEntry("minimalNextSlice", "people_document_backfill_reconciliation_preparation")
                .containsEntry("migrationAllowedNow", true)
                .containsEntry("backfillAllowedNow", true)
                .containsEntry("localReadAdapterAllowedNow", true)
                .containsEntry("localReadAdapterPrepared", true)
                .containsEntry("localReadCutoverAllowedNow", false)
                .containsEntry("reconciliationKey", "people_documento_read_model.id_pessoa_documento")
                .containsEntry("nextImplementationSlice", "phase_75_closure_adapter_prepared_no_activation");
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> documentSchemaMigration =
                (java.util.Map<String, Object>) peopleDocumentSchema.get("schemaMigration");
        assertThat(documentSchemaMigration)
                .containsEntry("version", "V5__create_people_document_metadata_read_model.sql")
                .containsEntry("enabledByDefault", false)
                .containsEntry("automaticBackfill", false);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> documentPreparedArtifacts =
                (java.util.Map<String, Object>) peopleDocumentSchema.get("preparedArtifacts");
        assertThat(documentPreparedArtifacts)
                .containsEntry("adapter", "JdbcPeopleDocumentMetadataLocalReadAdapter")
                .containsEntry("adapterCreated", true)
                .containsEntry("internalServiceConnected", true)
                .containsEntry("routeCreated", false);
        @SuppressWarnings("unchecked")
        Map<String, Object> documentAdapterPreparation =
                (Map<String, Object>) health.getDetails().get("peopleDocumentMetadataLocalAdapterPreparationDiagnostic");
        assertThat(documentAdapterPreparation)
                .containsEntry("phase", "Fase 75")
                .containsEntry("slice", "people_document_local_metadata_adapter_preparation")
                .containsEntry("status", "jdbc_local_adapter_prepared_internal_fallback_only")
                .containsEntry("recommendedNextStep",
                        "close_phase_75_and_plan_people_document_backfill_reconciliation_preparation")
                .containsEntry("minimalNextSlice", "people_document_backfill_reconciliation_preparation")
                .containsEntry("adapterImplementationAllowedNow", true)
                .containsEntry("adapterPrepared", true)
                .containsEntry("internalServiceConnected", true)
                .containsEntry("externalRouteCreated", false)
                .containsEntry("localReadCutoverAllowedNow", false)
                .containsEntry("candidateSource", "people_documento_read_model")
                .containsEntry("fallbackSource", "monolith_proxy")
                .containsEntry("routingOperation", "documentMetadataLocalRead")
                .containsEntry("schemaVersion", "V5__create_people_document_metadata_read_model.sql");
        @SuppressWarnings("unchecked")
        Map<String, Object> documentBackfillReconciliation =
                (Map<String, Object>) health.getDetails().get("peopleDocumentBackfillReconciliationDiagnostic");
        assertThat(documentBackfillReconciliation)
                .containsEntry("phase", "Fase 76")
                .containsEntry("slice", "people_document_backfill_reconciliation_preparation")
                .containsEntry("status", "document_metadata_backfill_reconciliation_prepared_no_read_cutover")
                .containsEntry("recommendedNextStep",
                        "close_phase_76_and_keep_document_local_read_blocked_until_green")
                .containsEntry("minimalNextSlice", "people_document_local_read_activation_eligibility")
                .containsEntry("migrationAllowedNow", true)
                .containsEntry("backfillAllowedNow", true)
                .containsEntry("reconciliationAllowedNow", true)
                .containsEntry("localReadCutoverAllowedNow", false)
                .containsEntry("source", "monolith_jdbc")
                .containsEntry("target", "people_documento_read_model")
                .containsEntry("reconciliationKey", "people_documento_read_model.id_pessoa_documento");
        @SuppressWarnings("unchecked")
        Map<String, Object> documentActivationEligibility =
                (Map<String, Object>) health.getDetails().get("peopleDocumentLocalReadActivationEligibilityDiagnostic");
        assertThat(documentActivationEligibility)
                .containsEntry("phase", "Fase 77")
                .containsEntry("slice", "people_document_local_read_activation_eligibility")
                .containsEntry("status", "internal_document_local_read_guarded_without_external_route")
                .containsEntry("recommendedNextStep",
                        "close_phase_77_and_only_consider_internal_document_usage_when_guard_is_green")
                .containsEntry("minimalNextSlice", "people_document_internal_usage_candidate")
                .containsEntry("internalServiceConnected", true)
                .containsEntry("localReadGuardPrepared", true)
                .containsEntry("localReadCutoverAllowedNow", true)
                .containsEntry("externalRouteCreated", false)
                .containsEntry("fallbackRequired", true)
                .containsEntry("routingOperation", "documentMetadataLocalRead")
                .containsEntry("shadowRoute", "internal-operation:PeopleDocumentMetadataLocalReadPort")
                .containsEntry("candidateSource", "people_documento_read_model")
                .containsEntry("selectedSource", "people_documento_read_model")
                .containsEntry("fallbackSource", "monolith_proxy")
                .containsEntry("reason", "local-document-metadata-read-eligible");
        @SuppressWarnings("unchecked")
        Map<String, Object> documentInternalUsageCandidate =
                (Map<String, Object>) health.getDetails().get("peopleDocumentInternalUsageCandidateDiagnostic");
        assertThat(documentInternalUsageCandidate)
                .containsEntry("phase", "Fase 78")
                .containsEntry("slice", "people_document_internal_usage_candidate_diagnostic")
                .containsEntry("status", "no_safe_internal_consumer_without_route_or_monolith_contract_change")
                .containsEntry("recommendedNextStep",
                        "close_phase_78_and_start_funcionario_diagnostic_instead_of_forcing_document_usage")
                .containsEntry("minimalNextSlice", "funcionario_minimal_diagnostic")
                .containsEntry("internalUsageCandidateFound", false)
                .containsEntry("safeToConnectNow", false)
                .containsEntry("externalRouteChangeRequired", false)
                .containsEntry("fallbackRequired", true);
        @SuppressWarnings("unchecked")
        Map<String, Object> documentScopeClosure =
                (Map<String, Object>) health.getDetails().get("peopleDocumentScopeClosureDiagnostic");
        assertThat(documentScopeClosure)
                .containsEntry("phase", "Fase 86")
                .containsEntry("slice", "people_document_metadata_scope_closure_review")
                .containsEntry("status", "people_document_metadata_scope_review_closed_ready_for_next_family_diagnostic")
                .containsEntry("recommendedNextStep",
                        "start_next_backend_family_without_reopening_people_document_metadata")
                .containsEntry("minimalNextSlice", "next_backend_family_diagnostic")
                .containsEntry("readScopeClosed", true)
                .containsEntry("writeScopePreparedWithoutCutover", false)
                .containsEntry("activationRequiredNow", false)
                .containsEntry("safeToStartNextFamilyDiagnostic", true);
        @SuppressWarnings("unchecked")
        Map<String, Object> documentScopeClosureRecommendation =
                (Map<String, Object>) documentScopeClosure.get("currentRecommendation");
        assertThat(documentScopeClosureRecommendation)
                .containsEntry("keepDocumentGuardPreparedButUnused", true)
                .containsEntry("keepDocumentAuthorityOnMonolith", true)
                .containsEntry("nextPreferredFamily", "next_backend_family")
                .containsEntry("reopenPessoaDocumentoInThisPhase", false);
        @SuppressWarnings("unchecked")
        Map<String, Object> documentAlunoConsumerContract =
                (Map<String, Object>) health.getDetails().get("peopleDocumentAlunoConsumerContractDiagnostic");
        assertThat(documentAlunoConsumerContract)
                .containsEntry("phase", "Fase 88")
                .containsEntry("slice", "people_document_aluno_consumer_contract")
                .containsEntry("status", "future_aluno_consumer_contract_prepared_without_route_or_legacy_change")
                .containsEntry("recommendedNextStep",
                        "prepare_people_document_aluno_consumer_connection_strategy_in_people_service_only")
                .containsEntry("minimalNextSlice", "people_document_aluno_consumer_connection_strategy")
                .containsEntry("firstFutureConsumer", "documento_aluno_listar_por_aluno")
                .containsEntry("consumerOperation", "listarDocumentosPorPessoa")
                .containsEntry("candidateSource", "people_document_read_model_candidate")
                .containsEntry("fallbackSource", "monolith_proxy")
                .containsEntry("contractPrepared", true)
                .containsEntry("localReadServiceReusable", true)
                .containsEntry("safeToConnectNow", false)
                .containsEntry("routeChangeRequiredNow", false)
                .containsEntry("monolithChangeRequiredNow", false)
                .containsEntry("fallbackRequired", true);
        @SuppressWarnings("unchecked")
        Map<String, Object> documentAlunoConsumerRecommendation =
                (Map<String, Object>) documentAlunoConsumerContract.get("currentRecommendation");
        assertThat(documentAlunoConsumerRecommendation)
                .containsEntry("keepConnectionInsidePeopleServicePlanning", true)
                .containsEntry("keepLegacyUntouched", true)
                .containsEntry("nextPreferredFamily", "people_document_aluno_consumer_connection_strategy")
                .containsEntry("connectConsumerNow", false);
        @SuppressWarnings("unchecked")
        Map<String, Object> documentAlunoConsumerConnectionStrategy =
                (Map<String, Object>) health.getDetails().get("peopleDocumentAlunoConsumerConnectionStrategyDiagnostic");
        assertThat(documentAlunoConsumerConnectionStrategy)
                .containsEntry("phase", "Fase 89")
                .containsEntry("slice", "people_document_aluno_consumer_connection_strategy")
                .containsEntry("status", "aluno_pessoa_local_resolution_prepared_without_real_consumer_connection")
                .containsEntry("recommendedNextStep",
                        "close_phase_89_and_keep_consumer_unconnected_until_a_new_service_flow_justifies_it")
                .containsEntry("minimalNextSlice", "people_document_aluno_consumer_connection_closure")
                .containsEntry("alunoPessoaLookupContractPrepared", true)
                .containsEntry("alunoPessoaLookupAdapterPrepared", true)
                .containsEntry("localResolutionReadyForConnection", true)
                .containsEntry("realConsumerConnected", false)
                .containsEntry("routeChangeRequiredNow", false)
                .containsEntry("legacyChangeRequiredNow", false)
                .containsEntry("fallbackRequired", true);
        @SuppressWarnings("unchecked")
        Map<String, Object> documentAlunoConsumerConnectionRecommendation =
                (Map<String, Object>) documentAlunoConsumerConnectionStrategy.get("currentRecommendation");
        assertThat(documentAlunoConsumerConnectionRecommendation)
                .containsEntry("closeCurrentMacroPhaseAfterThisStep", true)
                .containsEntry("keepConsumerUnconnected", true)
                .containsEntry("nextPreferredFamily", "people_document_aluno_consumer_connection_closure")
                .containsEntry("realFlowConnectionAllowedNow", false);
        @SuppressWarnings("unchecked")
        Map<String, Object> documentResponsavelConsumerContract =
                (Map<String, Object>) health.getDetails().get("peopleDocumentResponsavelConsumerContractDiagnostic");
        assertThat(documentResponsavelConsumerContract)
                .containsEntry("phase", "Fase 90")
                .containsEntry("slice", "people_document_responsavel_consumer_contract")
                .containsEntry("status", "future_responsavel_consumer_contract_prepared_without_route_or_legacy_change")
                .containsEntry("recommendedNextStep",
                        "prepare_people_document_responsavel_consumer_connection_strategy_in_people_service_only")
                .containsEntry("minimalNextSlice", "people_document_responsavel_consumer_connection_strategy")
                .containsEntry("firstFutureConsumer", "documento_responsavel_listar_por_responsavel")
                .containsEntry("consumerOperation", "listarDocumentosPorPessoa")
                .containsEntry("candidateSource", "people_document_read_model_candidate")
                .containsEntry("fallbackSource", "monolith_proxy")
                .containsEntry("contractPrepared", true)
                .containsEntry("localReadServiceReusable", true)
                .containsEntry("safeToConnectNow", false)
                .containsEntry("routeChangeRequiredNow", false)
                .containsEntry("legacyChangeRequiredNow", false)
                .containsEntry("fallbackRequired", true);
        @SuppressWarnings("unchecked")
        Map<String, Object> documentResponsavelConsumerRecommendation =
                (Map<String, Object>) documentResponsavelConsumerContract.get("currentRecommendation");
        assertThat(documentResponsavelConsumerRecommendation)
                .containsEntry("keepConnectionInsidePeopleServicePlanning", true)
                .containsEntry("keepLegacyUntouched", true)
                .containsEntry("nextPreferredFamily", "people_document_responsavel_consumer_connection_strategy")
                .containsEntry("connectConsumerNow", false);
        @SuppressWarnings("unchecked")
        Map<String, Object> documentResponsavelConsumerConnectionStrategy =
                (Map<String, Object>) health.getDetails().get("peopleDocumentResponsavelConsumerConnectionStrategyDiagnostic");
        assertThat(documentResponsavelConsumerConnectionStrategy)
                .containsEntry("phase", "Fase 91")
                .containsEntry("slice", "people_document_responsavel_consumer_connection_strategy")
                .containsEntry("status", "responsavel_pessoa_local_resolution_prepared_without_real_consumer_connection")
                .containsEntry("recommendedNextStep",
                        "close_phase_91_and_keep_responsavel_consumer_unconnected_until_a_new_service_flow_justifies_it")
                .containsEntry("minimalNextSlice", "people_document_responsavel_consumer_connection_closure")
                .containsEntry("responsavelPessoaLookupContractPrepared", true)
                .containsEntry("responsavelPessoaLookupAdapterPrepared", true)
                .containsEntry("localResolutionReadyForConnection", true)
                .containsEntry("realConsumerConnected", false)
                .containsEntry("routeChangeRequiredNow", false)
                .containsEntry("legacyChangeRequiredNow", false)
                .containsEntry("fallbackRequired", true);
        @SuppressWarnings("unchecked")
        Map<String, Object> documentResponsavelConsumerConnectionRecommendation =
                (Map<String, Object>) documentResponsavelConsumerConnectionStrategy.get("currentRecommendation");
        assertThat(documentResponsavelConsumerConnectionRecommendation)
                .containsEntry("closeCurrentMacroPhaseAfterThisStep", true)
                .containsEntry("keepConsumerUnconnected", true)
                .containsEntry("nextPreferredFamily", "people_document_responsavel_consumer_connection_closure")
                .containsEntry("realFlowConnectionAllowedNow", false);
        @SuppressWarnings("unchecked")
        Map<String, Object> funcionarioScopeDiagnostic =
                (Map<String, Object>) health.getDetails().get("peopleFuncionarioScopeDiagnostic");
        assertThat(funcionarioScopeDiagnostic)
                .containsEntry("phase", "Fase 79")
                .containsEntry("slice", "funcionario_contract_diagnostic")
                .containsEntry("status",
                        "employee_summary_contract_preferred_professor_and_auth_dependencies_preserved_on_monolith")
                .containsEntry("recommendedNextStep",
                        "prepare_minimal_funcionario_internal_summary_contract_without_route_or_persistence_cutover")
                .containsEntry("minimalNextSlice", "funcionario_internal_summary_contract")
                .containsEntry("diagnosticReadyNow", true)
                .containsEntry("internalContractSeparationAllowedNow", true)
                .containsEntry("localPersistenceAllowedNow", false)
                .containsEntry("externalRouteChangeAllowedNow", false)
                .containsEntry("fallbackToCurrentMonolithRequired", true);
        @SuppressWarnings("unchecked")
        Map<String, Object> funcionarioInternalSummaryContract =
                (Map<String, Object>) health.getDetails().get("peopleFuncionarioInternalSummaryContractDiagnostic");
        assertThat(funcionarioInternalSummaryContract)
                .containsEntry("phase", "Fase 80")
                .containsEntry("slice", "funcionario_internal_summary_contract")
                .containsEntry("status", "internal_contract_prepared_no_adapter_no_route")
                .containsEntry("recommendedNextStep",
                        "close_phase_80_and_plan_funcionario_internal_summary_adapter_preparation")
                .containsEntry("minimalNextSlice", "funcionario_internal_summary_adapter_preparation")
                .containsEntry("contractPrepared", true)
                .containsEntry("internalServicePrepared", true)
                .containsEntry("adapterCreated", false)
                .containsEntry("localPersistenceConnected", false)
                .containsEntry("externalRouteCreated", false)
                .containsEntry("fallbackRequired", true)
                .containsEntry("candidateSource", "people_funcionario_read_model_candidate")
                .containsEntry("fallbackSource", "monolith_internal_rh");
        @SuppressWarnings("unchecked")
        Map<String, Object> funcionarioInternalSummaryAdapterPreparation =
                (Map<String, Object>) health.getDetails().get("peopleFuncionarioInternalSummaryAdapterPreparationDiagnostic");
        assertThat(funcionarioInternalSummaryAdapterPreparation)
                .containsEntry("phase", "Fase 81")
                .containsEntry("slice", "funcionario_internal_summary_adapter_preparation")
                .containsEntry("status", "jdbc_local_adapter_prepared_internal_fallback_only")
                .containsEntry("recommendedNextStep",
                        "close_phase_81_and_plan_funcionario_internal_summary_backfill_reconciliation_preparation")
                .containsEntry("minimalNextSlice", "funcionario_internal_summary_backfill_reconciliation_preparation")
                .containsEntry("adapterImplementationAllowedNow", true)
                .containsEntry("adapterPrepared", true)
                .containsEntry("internalServiceConnected", true)
                .containsEntry("externalRouteCreated", false)
                .containsEntry("localReadCutoverAllowedNow", false)
                .containsEntry("candidateSource", "people_funcionario_read_model")
                .containsEntry("fallbackSource", "monolith_internal_rh")
                .containsEntry("routingOperation", "funcionarioInternalSummaryLocalRead")
                .containsEntry("schemaVersion", "V6__create_people_funcionario_internal_summary_read_model.sql");
        @SuppressWarnings("unchecked")
        Map<String, Object> funcionarioInternalSummaryBackfillReconciliation =
                (Map<String, Object>) health.getDetails().get("peopleFuncionarioInternalSummaryBackfillReconciliationDiagnostic");
        assertThat(funcionarioInternalSummaryBackfillReconciliation)
                .containsEntry("phase", "Fase 82")
                .containsEntry("slice", "funcionario_internal_summary_backfill_reconciliation_preparation")
                .containsEntry("status",
                        "funcionario_internal_summary_backfill_reconciliation_prepared_no_read_cutover")
                .containsEntry("recommendedNextStep",
                        "close_phase_82_and_keep_funcionario_internal_summary_local_read_blocked_until_green")
                .containsEntry("minimalNextSlice", "funcionario_internal_summary_local_read_activation_eligibility")
                .containsEntry("migrationAllowedNow", true)
                .containsEntry("backfillAllowedNow", true)
                .containsEntry("reconciliationAllowedNow", true)
                .containsEntry("localReadCutoverAllowedNow", false)
                .containsEntry("source", "monolith_jdbc")
                .containsEntry("target", "people_funcionario_read_model")
                .containsEntry("reconciliationKey", "people_funcionario_read_model.id_funcionario");
        @SuppressWarnings("unchecked")
        Map<String, Object> funcionarioInternalSummaryActivationEligibility =
                (Map<String, Object>) health.getDetails()
                        .get("peopleFuncionarioInternalSummaryLocalReadActivationEligibilityDiagnostic");
        assertThat(funcionarioInternalSummaryActivationEligibility)
                .containsEntry("phase", "Fase 83")
                .containsEntry("slice", "funcionario_internal_summary_local_read_activation_eligibility")
                .containsEntry("status", "internal_funcionario_local_read_guarded_without_external_route")
                .containsEntry("recommendedNextStep",
                        "close_phase_83_and_only_consider_funcionario_internal_usage_when_guard_is_green")
                .containsEntry("minimalNextSlice", "funcionario_internal_summary_internal_usage_candidate")
                .containsEntry("internalServiceConnected", true)
                .containsEntry("localReadGuardPrepared", true)
                .containsEntry("localReadCutoverAllowedNow", true)
                .containsEntry("externalRouteCreated", false)
                .containsEntry("fallbackRequired", true)
                .containsEntry("routingOperation", "funcionarioInternalSummaryLocalRead")
                .containsEntry("shadowRoute", "internal-operation:PeopleFuncionarioInternalSummaryPort")
                .containsEntry("candidateSource", "people_funcionario_read_model")
                .containsEntry("selectedSource", "people_funcionario_read_model")
                .containsEntry("fallbackSource", "monolith_internal_rh")
                .containsEntry("reason", "local-funcionario-internal-summary-read-eligible");
        @SuppressWarnings("unchecked")
        Map<String, Object> funcionarioInternalUsageCandidate =
                (Map<String, Object>) health.getDetails().get("peopleFuncionarioInternalUsageCandidateDiagnostic");
        assertThat(funcionarioInternalUsageCandidate)
                .containsEntry("phase", "Fase 84")
                .containsEntry("slice", "funcionario_internal_summary_internal_usage_candidate_diagnostic")
                .containsEntry("status", "no_safe_internal_funcionario_consumer_without_route_or_monolith_contract_change")
                .containsEntry("recommendedNextStep",
                        "close_phase_84_and_keep_funcionario_internal_summary_prepared_without_forced_consumer")
                .containsEntry("minimalNextSlice", "funcionario_internal_summary_block_closure")
                .containsEntry("internalUsageCandidateFound", false)
                .containsEntry("safeToConnectNow", false)
                .containsEntry("externalRouteChangeRequired", false)
                .containsEntry("fallbackRequired", true);
        @SuppressWarnings("unchecked")
        Map<String, Object> funcionarioScopeClosure =
                (Map<String, Object>) health.getDetails().get("peopleFuncionarioScopeClosureDiagnostic");
        assertThat(funcionarioScopeClosure)
                .containsEntry("phase", "Fase 85")
                .containsEntry("slice", "funcionario_internal_summary_scope_closure_review")
                .containsEntry("status", "funcionario_internal_summary_scope_review_closed_ready_for_next_family_diagnostic")
                .containsEntry("recommendedNextStep",
                        "start_next_backend_family_without_reopening_funcionario_internal_summary")
                .containsEntry("minimalNextSlice", "next_backend_family_diagnostic")
                .containsEntry("readScopeClosed", true)
                .containsEntry("writeScopePreparedWithoutCutover", false)
                .containsEntry("activationRequiredNow", false)
                .containsEntry("safeToStartNextFamilyDiagnostic", true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> funcionarioCurrentRecommendation =
                (java.util.Map<String, Object>) funcionarioScopeClosure.get("currentRecommendation");
        assertThat(funcionarioCurrentRecommendation)
                .containsEntry("keepFuncionarioGuardPreparedButUnused", true)
                .containsEntry("keepFuncionarioAuthorityOnMonolith", true)
                .containsEntry("nextPreferredFamily", "next_backend_family")
                .containsEntry("reopenFuncionarioInThisPhase", false);

        @SuppressWarnings("unchecked")
        Map<String, Object> contactPreparation =
                (Map<String, Object>) health.getDetails().get("peopleContactLocalReadPreparationDiagnostic");
        assertThat(contactPreparation)
                .containsEntry("phase", "Fase 92")
                .containsEntry("slice", "people_contact_local_read_preparation")
                .containsEntry("status", "internal_contact_read_prepared_with_local_adapter_no_route")
                .containsEntry("recommendedNextStep",
                        "close_phase_92_and_plan_people_contact_internal_consumer_diagnostic")
                .containsEntry("minimalNextSlice", "people_contact_internal_consumer_diagnostic")
                .containsEntry("contractPrepared", true)
                .containsEntry("internalServicePrepared", true)
                .containsEntry("adapterCreated", true)
                .containsEntry("localPersistenceConnected", true)
                .containsEntry("externalRouteCreated", false)
                .containsEntry("candidateSource", "pessoa")
                .containsEntry("fallbackSource", "monolith_proxy")
                .containsEntry("fallbackRequired", true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> preparedContactArtifacts =
                (java.util.Map<String, Object>) contactPreparation.get("preparedArtifacts");
        assertThat(preparedContactArtifacts)
                .containsEntry("port", "PeopleContactLocalReadPort")
                .containsEntry("response", "PessoaContatoLocalReadResponse")
                .containsEntry("adapter", "JdbcPeopleContactLocalReadAdapter")
                .containsEntry("internalService", "PeopleContactLocalReadService")
                .containsEntry("routeCreated", false)
                .containsEntry("adapterCreated", true)
                .containsEntry("localPersistenceConnected", true);
        @SuppressWarnings("unchecked")
        Map<String, Object> contactUsageCandidate =
                (Map<String, Object>) health.getDetails().get("peopleContactInternalUsageCandidateDiagnostic");
        assertThat(contactUsageCandidate)
                .containsEntry("phase", "Fase 93")
                .containsEntry("slice", "people_contact_internal_usage_candidate_diagnostic")
                .containsEntry("status", "no_safe_internal_contact_consumer_without_route_or_query_scope_change")
                .containsEntry("recommendedNextStep",
                        "close_phase_93_and_keep_contact_prepared_without_forced_consumer")
                .containsEntry("minimalNextSlice", "people_contact_block_closure")
                .containsEntry("internalUsageCandidateFound", false)
                .containsEntry("safeToConnectNow", false)
                .containsEntry("externalRouteChangeRequired", false)
                .containsEntry("fallbackRequired", true);
        @SuppressWarnings("unchecked")
        Map<String, Object> contactScopeClosure =
                (Map<String, Object>) health.getDetails().get("peopleContactScopeClosureDiagnostic");
        assertThat(contactScopeClosure)
                .containsEntry("phase", "Fase 94")
                .containsEntry("slice", "people_contact_scope_closure_review")
                .containsEntry("status", "people_contact_scope_review_closed_ready_for_next_family_diagnostic")
                .containsEntry("recommendedNextStep",
                        "start_next_backend_family_without_reopening_people_contact")
                .containsEntry("minimalNextSlice", "next_backend_family_diagnostic")
                .containsEntry("readScopeClosed", true)
                .containsEntry("writeScopePreparedWithoutCutover", false)
                .containsEntry("activationRequiredNow", false)
                .containsEntry("safeToStartNextFamilyDiagnostic", true);
        @SuppressWarnings("unchecked")
        Map<String, Object> professorScopeDiagnostic =
                (Map<String, Object>) health.getDetails().get("peopleProfessorScopeDiagnostic");
        assertThat(professorScopeDiagnostic)
                .containsEntry("phase", "Fase 95")
                .containsEntry("slice", "professor_contract_diagnostic")
                .containsEntry("status",
                        "professor_summary_contract_preferred_with_funcionario_and_person_authority_preserved_on_monolith")
                .containsEntry("recommendedNextStep",
                        "prepare_minimal_professor_internal_summary_contract_without_route_or_persistence_cutover")
                .containsEntry("minimalNextSlice", "professor_internal_summary_contract")
                .containsEntry("diagnosticReadyNow", true)
                .containsEntry("internalContractSeparationAllowedNow", true)
                .containsEntry("localPersistenceAllowedNow", false)
                .containsEntry("externalRouteChangeAllowedNow", false)
                .containsEntry("fallbackToCurrentMonolithRequired", true);
        @SuppressWarnings("unchecked")
        Map<String, Object> professorCurrentRecommendation =
                (Map<String, Object>) professorScopeDiagnostic.get("currentRecommendation");
        assertThat(professorCurrentRecommendation)
                .containsEntry("preferFirstImplementation", "professor_internal_summary_read_only")
                .containsEntry("keepProfessorReadsOnMonolith", true)
                .containsEntry("keepAcademicAllocationOutOfPeopleService", true)
                .containsEntry("prepareExternalRouteNow", false)
                .containsEntry("advanceToPersistenceNow", false);
        @SuppressWarnings("unchecked")
        Map<String, Object> professorInternalSummaryContract =
                (Map<String, Object>) health.getDetails().get("peopleProfessorInternalSummaryContractDiagnostic");
        assertThat(professorInternalSummaryContract)
                .containsEntry("phase", "Fase 95")
                .containsEntry("slice", "professor_internal_summary_contract")
                .containsEntry("status", "internal_contract_prepared_no_adapter_no_route")
                .containsEntry("recommendedNextStep",
                        "close_phase_95_and_plan_professor_internal_summary_adapter_preparation")
                .containsEntry("minimalNextSlice", "professor_internal_summary_adapter_preparation")
                .containsEntry("contractPrepared", true)
                .containsEntry("internalServicePrepared", true)
                .containsEntry("adapterCreated", false)
                .containsEntry("localPersistenceConnected", false)
                .containsEntry("externalRouteCreated", false)
                .containsEntry("fallbackRequired", true)
                .containsEntry("candidateSource", "people_professor_read_model_candidate")
                .containsEntry("fallbackSource", "monolith_internal_professor");
        @SuppressWarnings("unchecked")
        Map<String, Object> preparedProfessorArtifacts =
                (Map<String, Object>) professorInternalSummaryContract.get("preparedArtifacts");
        assertThat(preparedProfessorArtifacts)
                .containsEntry("port", "PeopleProfessorInternalSummaryPort")
                .containsEntry("response", "PessoaProfessorInternalSummaryResponse")
                .containsEntry("internalService", "PeopleProfessorInternalSummaryService")
                .containsEntry("adapterCreated", false)
                .containsEntry("routeCreated", false)
                .containsEntry("localPersistenceConnected", false);
        @SuppressWarnings("unchecked")
        Map<String, Object> professorAdapterPreparation =
                (Map<String, Object>) health.getDetails().get("peopleProfessorInternalSummaryAdapterPreparationDiagnostic");
        assertThat(professorAdapterPreparation)
                .containsEntry("phase", "Fase 95")
                .containsEntry("slice", "professor_internal_summary_adapter_preparation")
                .containsEntry("status", "adapter_preparation_diagnosed_contract_ready_keep_local_read_inactive")
                .containsEntry("recommendedNextStep",
                        "close_phase_95_and_formally_finish_professor_initial_block")
                .containsEntry("minimalNextSlice", "professor_initial_block_closure")
                .containsEntry("adapterImplementationAllowedNow", true)
                .containsEntry("adapterPrepared", false)
                .containsEntry("internalServiceConnected", true)
                .containsEntry("externalRouteCreated", false)
                .containsEntry("localReadCutoverAllowedNow", false)
                .containsEntry("candidateSource", "people_professor_read_model_candidate")
                .containsEntry("fallbackSource", "monolith_internal_professor")
                .containsEntry("routingOperation", "professorInternalSummaryLocalRead")
                .containsEntry("schemaVersion", "not-created-in-this-phase");
    }
}
