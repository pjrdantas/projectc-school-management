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
                .containsEntry("status", "command_contract_defined_no_write_cutover")
                .containsEntry("writeCutoverAllowedNow", false)
                .containsEntry("localReadPrerequisiteClosed", true)
                .containsEntry("recommendedNextStep", "evaluate_backend_shadow_command_without_local_persistence");
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> preparedCommandArtifacts =
                (java.util.Map<String, Object>) addressWriteAuthority.get("preparedCommandArtifacts");
        assertThat(preparedCommandArtifacts)
                .containsEntry("port", "PeopleAddressWritePort")
                .containsEntry("writeCommand", "PessoaEnderecoWriteCommand")
                .containsEntry("cleanupCommand", "PessoaEnderecoCleanupCommand")
                .containsEntry("result", "PessoaEnderecoWriteResult")
                .containsEntry("localPersistenceConnected", false);
        @SuppressWarnings("unchecked")
        java.util.List<String> consistencyBlockers =
                (java.util.List<String>) addressWriteAuthority.get("consistencyBlockers");
        assertThat(consistencyBlockers).contains(
                "address-write-is-coupled-to-person-create-update-transaction",
                "local-read-model-is-not-write-authority");
    }
}
