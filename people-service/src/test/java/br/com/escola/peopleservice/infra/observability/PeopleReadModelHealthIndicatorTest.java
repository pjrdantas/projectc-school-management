package br.com.escola.peopleservice.infra.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import br.com.escola.peopleservice.application.service.PeopleReadModelSyncState;
import br.com.escola.peopleservice.application.service.PeopleReadModelMigrationState;
import br.com.escola.peopleservice.application.service.PeopleReadSourcePolicy;
import br.com.escola.peopleservice.application.state.PeopleReadModelSyncSummary;
import br.com.escola.peopleservice.infra.config.PeopleReadModelProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PeopleReadModelHealthIndicatorTest {

    @Test
    void deveReportarModoMonolitoQuandoPersistenciaLocalEstaDesligada() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        Counter.builder("people.readmodel.sync.records")
                .tag("tabela", "pessoa")
                .tag("resultado", "success")
                .register(meterRegistry)
                .increment(3.0d);

        PeopleReadModelProperties properties =
                new PeopleReadModelProperties(false, false, false, false, false, false, 500, true);
        PeopleReadModelHealthIndicator indicator = new PeopleReadModelHealthIndicator(
                properties,
                meterRegistry,
                new PeopleReadSourcePolicy(properties, meterRegistry, new PeopleReadModelSyncState()),
                new PeopleReadModelMigrationState(),
                new PeopleReadModelSyncState());

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("enabled", false)
                .containsEntry("localReadRoutingEnabled", false)
                .containsEntry("operationalMode", "monolith_only")
                .containsEntry("authoritativeWriteStorage", false)
                .containsEntry("rollbackStrategy", "disable_people.read-model.enabled")
                .containsEntry("backfillRecordsTotal", 3.0d)
                .containsEntry("addressWriteCommandsTotal", 0.0d);

        @SuppressWarnings("unchecked")
        Map<String, Object> coreReadRoutes = (Map<String, Object>) health.getDetails().get("coreReadRoutes");
        @SuppressWarnings("unchecked")
        Map<String, Object> buscarPorId = (Map<String, Object>) coreReadRoutes.get("buscarPorId");
        assertThat(buscarPorId)
                .containsEntry("route", "GET /internal/v1/pessoas/{id}")
                .containsEntry("candidateSource", "pessoa")
                .containsEntry("selectedSource", "monolith_proxy")
                .containsEntry("localReadRequested", false)
                .containsEntry("localReadEligible", false)
                .containsEntry("reason", "local-read-routing-disabled");
    }

    @Test
    void deveFicarOutOfServiceQuandoRoteamentoLocalEstaAtivoMasSincronizacaoNaoEstaVerde() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleReadModelProperties properties =
                new PeopleReadModelProperties(true, false, true, false, true, true, 500, true);
        PeopleReadModelHealthIndicator indicator = new PeopleReadModelHealthIndicator(
                properties,
                meterRegistry,
                new PeopleReadSourcePolicy(properties, meterRegistry, new PeopleReadModelSyncState()),
                new PeopleReadModelMigrationState(),
                new PeopleReadModelSyncState());

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
        assertThat(health.getDetails())
                .containsEntry("localReadRoutingEnabled", true)
                .containsEntry("reason", "local-read-model-backfill-not-green");
    }

    @Test
    void deveFicarUpQuandoRoteamentoLocalEstaVerde() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleReadModelProperties properties =
                new PeopleReadModelProperties(true, false, true, false, true, true, 500, true);
        PeopleReadModelSyncState operationState = new PeopleReadModelSyncState();
        operationState.update(new PeopleReadModelSyncSummary(
                true,
                true,
                "completed",
                "local-read-model-backfill-and-reconciliation-completed",
                500,
                7,
                7,
                15,
                15,
                15,
                0,
                false,
                false,
                List.of()));

        PeopleReadModelHealthIndicator indicator = new PeopleReadModelHealthIndicator(
                properties,
                meterRegistry,
                new PeopleReadSourcePolicy(properties, meterRegistry, operationState),
                new PeopleReadModelMigrationState(),
                operationState);

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("localReadRoutingEnabled", true)
                .containsEntry("reason", "local-read-routing-eligible")
                .containsEntry("operationalMode", "local_read_model");

        @SuppressWarnings("unchecked")
        Map<String, Object> internalReadRoutes = (Map<String, Object>) health.getDetails().get("internalReadRoutes");
        @SuppressWarnings("unchecked")
        Map<String, Object> address = (Map<String, Object>) internalReadRoutes.get("address");
        @SuppressWarnings("unchecked")
        Map<String, Object> studentLink = (Map<String, Object>) internalReadRoutes.get("studentLink");
        @SuppressWarnings("unchecked")
        Map<String, Object> responsibleLink = (Map<String, Object>) internalReadRoutes.get("responsibleLink");
        assertThat(address)
                .containsEntry("route", "internal-operation:PessoaEnderecoPort")
                .containsEntry("selectedSource", "people_read_model_address")
                .containsEntry("localReadEligible", true)
                .containsEntry("reason", "local-address-read-eligible");
        assertThat(studentLink)
                .containsEntry("route", "internal-operation:AlunoPessoaPort")
                .containsEntry("selectedSource", "people_read_model_student_responsible")
                .containsEntry("localReadEligible", true)
                .containsEntry("reason", "local-student-link-read-eligible");
        assertThat(responsibleLink)
                .containsEntry("route", "internal-operation:ResponsavelPessoaPort")
                .containsEntry("selectedSource", "people_read_model_student_responsible")
                .containsEntry("localReadEligible", true)
                .containsEntry("reason", "local-responsible-link-read-eligible");
    }
}

