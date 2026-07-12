package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import br.com.escola.peopleservice.application.dto.PessoaResponsavelVinculoResponse;
import br.com.escola.peopleservice.application.port.out.ResponsavelPessoaPort;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class ResponsavelPessoaServiceTest {

    @Test
    void retornaVazioQuandoGuardBloqueiaLeituraLocal() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        ResponsavelPessoaService service = new ResponsavelPessoaService(
                provider(null),
                readRoutingPolicy(false, meterRegistry),
                meterRegistry);

        var response = service.buscarVinculoPorResponsavelId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.responsible.lookup",
                "operation", "buscarVinculoPorResponsavelId",
                "result", "guard_blocked").count()).isEqualTo(1.0d);
    }

    @Test
    void consultaAdapterQuandoDisponivel() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaResponsavelVinculoResponse vinculo = new PessoaResponsavelVinculoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID());
        ResponsavelPessoaService service = new ResponsavelPessoaService(
                provider(new CountingPort(vinculo)),
                readRoutingPolicy(true, meterRegistry),
                meterRegistry);

        var response = service.buscarVinculoPorResponsavelId(vinculo.responsavelId(), vinculo.escolaId());

        assertThat(response).contains(vinculo);
        assertThat(meterRegistry.counter(
                "people.responsible.lookup",
                "operation", "buscarVinculoPorResponsavelId",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void registraErroQuandoAdapterFalha() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        ResponsavelPessoaService service = new ResponsavelPessoaService(
                provider(new FailingPort()),
                readRoutingPolicy(true, meterRegistry),
                meterRegistry);

        var response = service.buscarVinculoPorResponsavelId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.responsible.lookup",
                "operation", "buscarVinculoPorResponsavelId",
                "result", "error").count()).isEqualTo(1.0d);
    }

    @Test
    void retornaVazioQuandoAdapterNaoExisteMesmoComGuardLiberado() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        ResponsavelPessoaService service = new ResponsavelPessoaService(
                provider(null),
                readRoutingPolicy(true, meterRegistry),
                meterRegistry);

        var response = service.buscarVinculoPorResponsavelId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.responsible.lookup",
                "operation", "buscarVinculoPorResponsavelId",
                "result", "adapter_missing").count()).isEqualTo(1.0d);
    }

    private PeopleReadSourcePolicy readRoutingPolicy(boolean localReadEligible, SimpleMeterRegistry meterRegistry) {
        return new PeopleReadSourcePolicy(
                new br.com.escola.peopleservice.infra.config.PeopleReadModelProperties(
                        localReadEligible,
                        false,
                        localReadEligible,
                        false,
                        localReadEligible,
                        localReadEligible,
                        500,
                        true),
                meterRegistry,
                localReadEligible ? greenState() : new PeopleReadModelSyncState());
    }

    private PeopleReadModelSyncState greenState() {
        PeopleReadModelSyncState state = new PeopleReadModelSyncState();
        state.update(new br.com.escola.peopleservice.application.state.PeopleReadModelSyncSummary(
                true,
                true,
                "completed",
                "local-read-model-backfill-and-reconciliation-completed",
                500,
                8,
                8,
                25,
                25,
                25,
                0,
                false,
                false,
                java.util.List.of()));
        return state;
    }

    private ObjectProvider<ResponsavelPessoaPort> provider(ResponsavelPessoaPort port) {
        return new ObjectProvider<>() {
            @Override
            public ResponsavelPessoaPort getObject(Object... args) {
                return port;
            }

            @Override
            public ResponsavelPessoaPort getIfAvailable() {
                return port;
            }

            @Override
            public ResponsavelPessoaPort getIfUnique() {
                return port;
            }

            @Override
            public ResponsavelPessoaPort getObject() {
                return port;
            }
        };
    }

    private static class CountingPort implements ResponsavelPessoaPort {

        private final PessoaResponsavelVinculoResponse vinculo;

        private CountingPort(PessoaResponsavelVinculoResponse vinculo) {
            this.vinculo = vinculo;
        }

        @Override
        public Optional<PessoaResponsavelVinculoResponse> buscarVinculoPorResponsavelId(UUID responsavelId, UUID escolaId) {
            return Optional.of(vinculo);
        }
    }

    private static class FailingPort implements ResponsavelPessoaPort {

        @Override
        public Optional<PessoaResponsavelVinculoResponse> buscarVinculoPorResponsavelId(UUID responsavelId, UUID escolaId) {
            throw new IllegalStateException("lookup failed");
        }
    }
}

