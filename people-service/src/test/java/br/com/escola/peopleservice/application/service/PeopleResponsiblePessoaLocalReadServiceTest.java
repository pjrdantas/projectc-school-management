package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import br.com.escola.peopleservice.application.dto.PessoaResponsavelVinculoResponse;
import br.com.escola.peopleservice.application.port.out.PeopleResponsiblePessoaLocalReadPort;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PeopleResponsiblePessoaLocalReadServiceTest {

    @Test
    void retornaVazioQuandoAdapterNaoExiste() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleResponsiblePessoaLocalReadService service = new PeopleResponsiblePessoaLocalReadService(
                provider(null),
                meterRegistry);

        var response = service.buscarVinculoPorResponsavelId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.responsible.pessoa.lookup",
                "operation", "buscarVinculoPorResponsavelId",
                "result", "adapter_missing").count()).isEqualTo(1.0d);
    }

    @Test
    void consultaAdapterQuandoDisponivel() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaResponsavelVinculoResponse vinculo = new PessoaResponsavelVinculoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID());
        PeopleResponsiblePessoaLocalReadService service = new PeopleResponsiblePessoaLocalReadService(
                provider(new CountingPort(vinculo)),
                meterRegistry);

        var response = service.buscarVinculoPorResponsavelId(vinculo.responsavelId(), vinculo.escolaId());

        assertThat(response).contains(vinculo);
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.responsible.pessoa.lookup",
                "operation", "buscarVinculoPorResponsavelId",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void registraErroQuandoAdapterFalha() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleResponsiblePessoaLocalReadService service = new PeopleResponsiblePessoaLocalReadService(
                provider(new FailingPort()),
                meterRegistry);

        var response = service.buscarVinculoPorResponsavelId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.responsible.pessoa.lookup",
                "operation", "buscarVinculoPorResponsavelId",
                "result", "error").count()).isEqualTo(1.0d);
    }

    private ObjectProvider<PeopleResponsiblePessoaLocalReadPort> provider(PeopleResponsiblePessoaLocalReadPort port) {
        return new ObjectProvider<>() {
            @Override
            public PeopleResponsiblePessoaLocalReadPort getObject(Object... args) {
                return port;
            }

            @Override
            public PeopleResponsiblePessoaLocalReadPort getIfAvailable() {
                return port;
            }

            @Override
            public PeopleResponsiblePessoaLocalReadPort getIfUnique() {
                return port;
            }

            @Override
            public PeopleResponsiblePessoaLocalReadPort getObject() {
                return port;
            }
        };
    }

    private static class CountingPort implements PeopleResponsiblePessoaLocalReadPort {

        private final PessoaResponsavelVinculoResponse vinculo;

        private CountingPort(PessoaResponsavelVinculoResponse vinculo) {
            this.vinculo = vinculo;
        }

        @Override
        public Optional<PessoaResponsavelVinculoResponse> buscarVinculoPorResponsavelId(UUID responsavelId, UUID escolaId) {
            return Optional.of(vinculo);
        }
    }

    private static class FailingPort implements PeopleResponsiblePessoaLocalReadPort {

        @Override
        public Optional<PessoaResponsavelVinculoResponse> buscarVinculoPorResponsavelId(UUID responsavelId, UUID escolaId) {
            throw new IllegalStateException("lookup failed");
        }
    }
}
