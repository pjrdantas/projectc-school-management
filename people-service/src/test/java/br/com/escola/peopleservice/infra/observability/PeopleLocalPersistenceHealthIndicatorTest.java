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
                .containsEntry("addressWriteShadowCommandsTotal", 0.0d)
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
                        "pessoa_endereco");

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
                .containsEntry("shadowService", "PeopleAddressWriteShadowService")
                .containsEntry("adapterCreated", true)
                .containsEntry("routeCreated", false)
                .containsEntry("localPersistenceConnected", false);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> shadowCommandExecution =
                (java.util.Map<String, Object>) addressWriteAuthority.get("shadowCommandExecution");
        assertThat(shadowCommandExecution)
                .containsEntry("service", "PeopleAddressWriteShadowService")
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
                "PeopleAddressWriteShadowService records shadow decisions without writing local tables");
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
                .containsEntry("status", "monolith_http_write_contract_missing_adapter_blocked")
                .containsEntry("recommendedNextStep",
                        "define_monolith_internal_address_write_http_contract_before_adapter")
                .containsEntry("minimalNextSlice", "monolith_internal_address_write_contract_no_people_adapter")
                .containsEntry("monolithHttpWriteContractAvailable", false)
                .containsEntry("adapterImplementationAllowedNow", false)
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
                .containsEntry("shadowService", "PeopleAddressWriteShadowService")
                .containsEntry("writePort", "PeopleAddressWritePort")
                .containsEntry("monolithWriteClientCreated", false)
                .containsEntry("localPersistenceConnected", false)
                .containsEntry("routeCreated", false);
        @SuppressWarnings("unchecked")
        java.util.List<String> adapterOutOfScope =
                (java.util.List<String>) addressWriteMonolithAdapter.get("explicitlyOutOfScope");
        assertThat(adapterOutOfScope).contains(
                "implement-people-service-monolith-write-client",
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
    }
}
