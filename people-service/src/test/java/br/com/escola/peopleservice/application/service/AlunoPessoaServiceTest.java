package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import br.com.escola.peopleservice.application.dto.PessoaAlunoVinculoResponse;
import br.com.escola.peopleservice.application.port.out.AlunoPessoaPort;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class AlunoPessoaServiceTest {

    @Test
    void retornaVazioQuandoAdapterNaoExiste() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AlunoPessoaService service = new AlunoPessoaService(
                provider(null),
                meterRegistry);

        var response = service.buscarVinculoPorAlunoId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.student.lookup",
                "operation", "buscarVinculoPorAlunoId",
                "result", "adapter_missing").count()).isEqualTo(1.0d);
    }

    @Test
    void consultaAdapterQuandoDisponivel() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaAlunoVinculoResponse vinculo = new PessoaAlunoVinculoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID());
        AlunoPessoaService service = new AlunoPessoaService(
                provider(new CountingPort(vinculo)),
                meterRegistry);

        var response = service.buscarVinculoPorAlunoId(vinculo.alunoId(), vinculo.escolaId());

        assertThat(response).contains(vinculo);
        assertThat(meterRegistry.counter(
                "people.student.lookup",
                "operation", "buscarVinculoPorAlunoId",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void registraErroQuandoAdapterFalha() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AlunoPessoaService service = new AlunoPessoaService(
                provider(new FailingPort()),
                meterRegistry);

        var response = service.buscarVinculoPorAlunoId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.student.lookup",
                "operation", "buscarVinculoPorAlunoId",
                "result", "error").count()).isEqualTo(1.0d);
    }

    private ObjectProvider<AlunoPessoaPort> provider(AlunoPessoaPort port) {
        return new ObjectProvider<>() {
            @Override
            public AlunoPessoaPort getObject(Object... args) {
                return port;
            }

            @Override
            public AlunoPessoaPort getIfAvailable() {
                return port;
            }

            @Override
            public AlunoPessoaPort getIfUnique() {
                return port;
            }

            @Override
            public AlunoPessoaPort getObject() {
                return port;
            }
        };
    }

    private static class CountingPort implements AlunoPessoaPort {

        private final PessoaAlunoVinculoResponse vinculo;

        private CountingPort(PessoaAlunoVinculoResponse vinculo) {
            this.vinculo = vinculo;
        }

        @Override
        public Optional<PessoaAlunoVinculoResponse> buscarVinculoPorAlunoId(UUID alunoId, UUID escolaId) {
            return Optional.of(vinculo);
        }
    }

    private static class FailingPort implements AlunoPessoaPort {

        @Override
        public Optional<PessoaAlunoVinculoResponse> buscarVinculoPorAlunoId(UUID alunoId, UUID escolaId) {
            throw new IllegalStateException("lookup failed");
        }
    }
}

