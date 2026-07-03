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
                .containsEntry("status", "schema_migration_opt_in_prepared_backfill_still_blocked")
                .containsEntry("implementationAllowedNow", false)
                .containsEntry("schemaAllowedNow", true)
                .containsEntry("backfillAllowedNow", false)
                .containsEntry("localReadCutoverAllowedNow", false)
                .containsEntry("dependsOnClosedSlice", "consultarCadastro")
                .containsEntry("firstSafeImplementationSlice", "address_backfill_reconciliation_diagnostic_no_cutover");
        @SuppressWarnings("unchecked")
        Map<String, Object> preparedInternalContract =
                (Map<String, Object>) nextBlockedSlice.get("preparedInternalContract");
        assertThat(preparedInternalContract)
                .containsEntry("port", "PessoaEnderecoPort")
                .containsEntry("jpaEntityExposure", false);
        @SuppressWarnings("unchecked")
        java.util.List<String> writeConsumers = (java.util.List<String>) nextBlockedSlice.get("writeConsumers");
        assertThat(writeConsumers).contains("PessoaFoundationService.atualizarPessoaEEndereco");

        @SuppressWarnings("unchecked")
        Map<String, Object> addressDiagnostic =
                (Map<String, Object>) details.get("addressSchemaBackfillDiagnostic");
        assertThat(addressDiagnostic)
                .containsEntry("slice", "endereco_pessoa_endereco")
                .containsEntry("status", "schema_migration_opt_in_prepared_backfill_still_blocked")
                .containsEntry("migrationAllowedNow", true)
                .containsEntry("backfillAllowedNow", false)
                .containsEntry("localReadCutoverAllowedNow", false)
                .containsEntry("reconciliationKey", "pessoa_endereco.id_pessoa_endereco")
                .containsEntry("nextImplementationSlice", "address_backfill_reconciliation_diagnostic_no_cutover");
        @SuppressWarnings("unchecked")
        Map<String, Object> schemaMigration = (Map<String, Object>) addressDiagnostic.get("schemaMigration");
        assertThat(schemaMigration)
                .containsEntry("version", "V4__create_people_address_read_model.sql")
                .containsEntry("automaticBackfill", false);
    }
}
