package br.com.escola.peopleservice.infra.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "management.endpoint.health.show-details=always")
class PeopleLocalPersistenceHealthEndpointIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    void deveExporHealthDedicadoDaFundacaoLocalDesligada() {
        RestClient client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();

        Map<?, ?> health = client.get()
                .uri("/actuator/health/peopleLocalPersistence")
                .retrieve()
                .body(Map.class);

        assertThat(health).isNotNull();
        assertThat(health.get("status")).isEqualTo("UP");

        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) health.get("details");
        assertThat(details)
                .containsEntry("enabled", false)
                .containsEntry("migrationEnabled", false)
                .containsEntry("readModelCutoverEnabled", false)
                .containsEntry("writeCutoverAllowed", false)
                .containsEntry("mode", "read_only_shadow_foundation");

        @SuppressWarnings("unchecked")
        Map<String, Object> shadowReadRoutes = (Map<String, Object>) details.get("shadowReadRoutes");
        assertThat(shadowReadRoutes).containsKeys(
                "listarTiposPessoa",
                "listarTiposEndereco",
                "buscarPorId",
                "consultarCadastro");
        assertThat(shadowReadRoutes).doesNotContainKey("addressLocalRead");

        @SuppressWarnings("unchecked")
        Map<String, Object> closure = (Map<String, Object>) details.get("guardedReadCutoverClosure");
        assertThat(closure)
                .containsEntry("operation", "consultarCadastro")
                .containsEntry("selectedSource", "monolith_proxy")
                .containsEntry("localCandidateSource", "people_read_model_student_responsible")
                .containsEntry("fallbackRequired", true)
                .containsEntry("nextSliceBlocked", "endereco");

        @SuppressWarnings("unchecked")
        Map<String, Object> nextBlockedSlice = (Map<String, Object>) details.get("nextBlockedSliceDiagnostic");
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
        Map<String, Object> preparedInternalContract =
                (Map<String, Object>) nextBlockedSlice.get("preparedInternalContract");
        assertThat(preparedInternalContract)
                .containsEntry("port", "PeopleAddressLocalReadPort")
                .containsEntry("response", "PessoaEnderecoLocalReadResponse")
                .containsEntry("adapter", "JdbcPeopleAddressLocalReadAdapter")
                .containsEntry("internalService", "PeopleAddressLocalReadService")
                .containsEntry("routingOperation", "addressLocalRead")
                .containsEntry("jpaEntityExposure", false);
        @SuppressWarnings("unchecked")
        java.util.List<String> writeConsumers = (java.util.List<String>) nextBlockedSlice.get("writeConsumers");
        assertThat(writeConsumers).contains("PessoaFoundationService.atualizarPessoaEEndereco");

        @SuppressWarnings("unchecked")
        Map<String, Object> addressDiagnostic =
                (Map<String, Object>) details.get("addressSchemaBackfillDiagnostic");
        assertThat(addressDiagnostic)
                .containsEntry("slice", "endereco_pessoa_endereco")
                .containsEntry("status", "backfill_reconciliation_prepared_read_cutover_still_blocked")
                .containsEntry("migrationAllowedNow", true)
                .containsEntry("backfillAllowedNow", true)
                .containsEntry("localReadCutoverAllowedNow", false)
                .containsEntry("reconciliationKey", "pessoa_endereco.id_pessoa_endereco")
                .containsEntry("nextImplementationSlice", "phase_68_closure_no_cutover");
        @SuppressWarnings("unchecked")
        Map<String, Object> schemaMigration = (Map<String, Object>) addressDiagnostic.get("schemaMigration");
        assertThat(schemaMigration)
                .containsEntry("version", "V4__create_people_address_read_model.sql")
                .containsEntry("automaticBackfill", false);
        @SuppressWarnings("unchecked")
        Map<String, Object> backfillReconciliation =
                (Map<String, Object>) addressDiagnostic.get("backfillReconciliation");
        assertThat(backfillReconciliation)
                .containsEntry("source", "monolith_jdbc")
                .containsEntry("target", "people_read_model_address");

        @SuppressWarnings("unchecked")
        Map<String, Object> addressLocalReadContract =
                (Map<String, Object>) details.get("addressLocalReadContractDiagnostic");
        assertThat(addressLocalReadContract)
                .containsEntry("slice", "endereco_local_read_contract")
                .containsEntry("status", "adapter_connected_to_internal_guard_no_route")
                .containsEntry("phase", "Fase 69")
                .containsEntry("contractAllowedNow", true)
                .containsEntry("localReadAdapterAllowedNow", false)
                .containsEntry("localReadAdapterPrepared", true)
                .containsEntry("localReadAdapterConnected", true)
                .containsEntry("localReadCutoverAllowedNow", false)
                .containsEntry("candidateSource", "people_read_model_address")
                .containsEntry("fallbackSource", "monolith_proxy")
                .containsEntry("nextImplementationSlice",
                        "address_adapter_connected_internal_guard_no_external_route");
        @SuppressWarnings("unchecked")
        Map<String, Object> preparedArtifacts =
                (Map<String, Object>) addressLocalReadContract.get("preparedArtifacts");
        assertThat(preparedArtifacts)
                .containsEntry("port", "PeopleAddressLocalReadPort")
                .containsEntry("response", "PessoaEnderecoLocalReadResponse")
                .containsEntry("adapter", "JdbcPeopleAddressLocalReadAdapter")
                .containsEntry("internalService", "PeopleAddressLocalReadService")
                .containsEntry("adapterCreated", true)
                .containsEntry("queryServiceConnected", false);

        @SuppressWarnings("unchecked")
        Map<String, Object> addressCutoverEligibility =
                (Map<String, Object>) details.get("addressLocalReadCutoverEligibilityDiagnostic");
        assertThat(addressCutoverEligibility)
                .containsEntry("slice", "endereco_read_cutover_eligibility")
                .containsEntry("phase", "Fase 70")
                .containsEntry("status", "adapter_connected_to_internal_guard_no_route")
                .containsEntry("routingOperation", "addressLocalRead")
                .containsEntry("shadowRoute", "internal-operation:PeopleAddressLocalReadPort")
                .containsEntry("selectedSource", "monolith_proxy")
                .containsEntry("localReadEligible", false)
                .containsEntry("localReadCutoverAllowedNow", false)
                .containsEntry("adapterPrepared", true)
                .containsEntry("internalGuardedServiceConnected", true)
                .containsEntry("queryServiceConnected", false)
                .containsEntry("routeCreated", false)
                .containsEntry("bffFrontendChangeAllowedNow", false)
                .containsEntry("writeCutoverAllowedNow", false)
                .containsEntry("fallbackRequired", true);

        @SuppressWarnings("unchecked")
        Map<String, Object> addressWriteAuthority =
                (Map<String, Object>) details.get("addressWriteAuthorityDiagnostic");
        assertThat(addressWriteAuthority)
                .containsEntry("phase", "Fase 71")
                .containsEntry("slice", "endereco_write_authority")
                .containsEntry("status", "backend_shadow_command_service_prepared_no_write_cutover")
                .containsEntry("writeCutoverAllowedNow", false)
                .containsEntry("localReadPrerequisiteClosed", true)
                .containsEntry("recommendedNextStep", "close_phase_71_or_plan_monolith_write_adapter_diagnostic");
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
                .containsEntry("localPersistenceConnected", false);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> shadowCommandExecution =
                (java.util.Map<String, Object>) addressWriteAuthority.get("shadowCommandExecution");
        assertThat(shadowCommandExecution)
                .containsEntry("service", "PeopleAddressWriteShadowService")
                .containsEntry("selectedSource", "monolith_proxy")
                .containsEntry("persistedLocally", false)
                .containsEntry("fallbackRequired", true)
                .containsEntry("localWriteEnabled", false);
        @SuppressWarnings("unchecked")
        java.util.List<String> consistencyBlockers =
                (java.util.List<String>) addressWriteAuthority.get("consistencyBlockers");
        assertThat(consistencyBlockers).contains(
                "address-write-is-coupled-to-person-create-update-transaction",
                "local-read-model-is-not-write-authority");

        @SuppressWarnings("unchecked")
        Map<String, Object> addressWriteMonolithAdapter =
                (Map<String, Object>) details.get("addressWriteMonolithAdapterDiagnostic");
        assertThat(addressWriteMonolithAdapter)
                .containsEntry("phase", "Fase 72")
                .containsEntry("slice", "address_write_monolith_adapter_diagnostic")
                .containsEntry("status", "monolith_write_adapter_prepared_guard_disabled_no_cutover")
                .containsEntry("monolithHttpWriteContractAvailable", true)
                .containsEntry("adapterImplementationAllowedNow", true)
                .containsEntry("writeCutoverAllowedNow", false)
                .containsEntry("localPersistenceAllowedNow", false)
                .containsEntry("recommendedNextStep",
                        "close_phase_72_and_plan_next_people_backend_scope");
        @SuppressWarnings("unchecked")
        java.util.List<String> requiredMonolithContracts =
                (java.util.List<String>) addressWriteMonolithAdapter.get("requiredMonolithContracts");
        assertThat(requiredMonolithContracts).contains(
                "PUT /internal/pessoas/{pessoaId}/endereco-principal",
                "DELETE /internal/pessoas/{pessoaId}/enderecos");
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> currentPeopleServiceState =
                (java.util.Map<String, Object>) addressWriteMonolithAdapter.get("currentPeopleServiceState");
        assertThat(currentPeopleServiceState)
                .containsEntry("shadowService", "PeopleAddressWriteShadowService")
                .containsEntry("monolithWriteClientCreated", true)
                .containsEntry("monolithWriteClient", "MonolithPessoaAddressWriteClient")
                .containsEntry("monolithWriteClientEnabledByDefault", false)
                .containsEntry("guardProperty", "people.shadow.monolith.address-write-adapter-enabled")
                .containsEntry("localPersistenceConnected", false)
                .containsEntry("routeCreated", false);

        @SuppressWarnings("unchecked")
        Map<String, Object> addressScopeClosure =
                (Map<String, Object>) details.get("peopleAddressScopeClosureDiagnostic");
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
        java.util.Map<String, Object> currentRecommendation =
                (java.util.Map<String, Object>) addressScopeClosure.get("currentRecommendation");
        assertThat(currentRecommendation)
                .containsEntry("keepAddressGuardDisabled", true)
                .containsEntry("keepAddressWritesOnMonolith", true)
                .containsEntry("nextPreferredFamily", "pessoa_documento")
                .containsEntry("reopenAddressInThisPhase", false);

        @SuppressWarnings("unchecked")
        Map<String, Object> peopleDocumentScope =
                (Map<String, Object>) details.get("peopleDocumentScopeDiagnostic");
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
        java.util.List<String> documentMonolithDependencies =
                (java.util.List<String>) peopleDocumentScope.get("monolithDependencies");
        assertThat(documentMonolithDependencies).contains(
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
                (Map<String, Object>) details.get("peopleDocumentInternalMetadataReadContractDiagnostic");
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
                (Map<String, Object>) details.get("peopleDocumentLocalReadCandidateDiagnostic");
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
                (Map<String, Object>) details.get("peopleDocumentMetadataSchemaDiagnostic");
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
                (Map<String, Object>) details.get("peopleDocumentMetadataLocalAdapterPreparationDiagnostic");
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
                (Map<String, Object>) details.get("peopleDocumentBackfillReconciliationDiagnostic");
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
                (Map<String, Object>) details.get("peopleDocumentLocalReadActivationEligibilityDiagnostic");
        assertThat(documentActivationEligibility)
                .containsEntry("phase", "Fase 77")
                .containsEntry("slice", "people_document_local_read_activation_eligibility")
                .containsEntry("status", "internal_document_local_read_guarded_without_external_route")
                .containsEntry("recommendedNextStep",
                        "close_phase_77_and_only_consider_internal_document_usage_when_guard_is_green")
                .containsEntry("minimalNextSlice", "people_document_internal_usage_candidate")
                .containsEntry("internalServiceConnected", true)
                .containsEntry("localReadGuardPrepared", true)
                .containsEntry("localReadCutoverAllowedNow", false)
                .containsEntry("externalRouteCreated", false)
                .containsEntry("fallbackRequired", true)
                .containsEntry("routingOperation", "documentMetadataLocalRead")
                .containsEntry("shadowRoute", "internal-operation:PeopleDocumentMetadataLocalReadPort")
                .containsEntry("candidateSource", "people_documento_read_model")
                .containsEntry("selectedSource", "monolith_proxy")
                .containsEntry("fallbackSource", "monolith_proxy")
                .containsEntry("reason", "read-model-cutover-disabled");
        @SuppressWarnings("unchecked")
        Map<String, Object> documentInternalUsageCandidate =
                (Map<String, Object>) details.get("peopleDocumentInternalUsageCandidateDiagnostic");
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
    }
}
