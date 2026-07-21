package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import br.com.escola.peopleservice.application.dto.PessoaContatoResponse;
import br.com.escola.peopleservice.application.port.out.PessoaContatoPort;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PessoaContatoServiceTest {

    @Test
    void retornaVazioQuandoGuardBloqueiaLeituraLocal() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaContatoService service = new PessoaContatoService(
                provider(null),
                readRoutingPolicy(false, meterRegistry),
                meterRegistry);

        Optional<PessoaContatoResponse> response =
                service.buscarContatoPorPessoa(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.contact.reads",
                "operation", "buscarContatoPorPessoa",
                "result", "fallback_guard_blocked").count()).isEqualTo(1.0d);
    }

    @Test
    void consultaAdapterQuandoDisponivel() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaContatoResponse contato = contato();
        CountingContatoPort port = new CountingContatoPort(contato);
        PessoaContatoService service = new PessoaContatoService(
                provider(port),
                readRoutingPolicy(true, meterRegistry),
                meterRegistry);

        Optional<PessoaContatoResponse> response =
                service.buscarContatoPorPessoa(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).contains(contato);
        assertThat(port.calls).isEqualTo(1);
        assertThat(meterRegistry.counter(
                "people.contact.reads",
                "operation", "buscarContatoPorPessoa",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void registraFallbackQuandoAdapterFalha() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaContatoService service =
                new PessoaContatoService(
                        provider(new FailingContatoPort()),
                        readRoutingPolicy(true, meterRegistry),
                        meterRegistry);

        Optional<PessoaContatoResponse> response =
                service.buscarContatoPorPessoa(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.contact.reads",
                "operation", "buscarContatoPorPessoa",
                "result", "fallback_error").count()).isEqualTo(1.0d);
    }

    @Test
    void retornaVazioQuandoAdapterNaoExisteMesmoComGuardLiberado() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaContatoService service = new PessoaContatoService(
                provider(null),
                readRoutingPolicy(true, meterRegistry),
                meterRegistry);

        Optional<PessoaContatoResponse> response =
                service.buscarContatoPorPessoa(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.contact.reads",
                "operation", "buscarContatoPorPessoa",
                "result", "fallback_adapter_missing").count()).isEqualTo(1.0d);
    }

    private PeopleDataAccessPolicy readRoutingPolicy(boolean localReadEligible, SimpleMeterRegistry meterRegistry) {
        return new PeopleDataAccessPolicy(
                new br.com.escola.peopleservice.infra.config.PeopleRuntimeProperties(
                        localReadEligible,
                        false,
                        localReadEligible,
                        false,
                        false,
                        false,
                        500,
                        false),
                meterRegistry);
    }

    private ObjectProvider<PessoaContatoPort> provider(PessoaContatoPort port) {
        return new ObjectProvider<>() {
            @Override
            public PessoaContatoPort getObject(Object... args) {
                return port;
            }

            @Override
            public PessoaContatoPort getIfAvailable() {
                return port;
            }

            @Override
            public PessoaContatoPort getIfUnique() {
                return port;
            }

            @Override
            public PessoaContatoPort getObject() {
                return port;
            }
        };
    }

    private PessoaContatoResponse contato() {
        return new PessoaContatoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ana.aluna@example.com",
                "11999999999",
                true);
    }

    private static class CountingContatoPort implements PessoaContatoPort {

        private final PessoaContatoResponse contato;
        private int calls;

        private CountingContatoPort(PessoaContatoResponse contato) {
            this.contato = contato;
        }

        @Override
        public Optional<PessoaContatoResponse> buscarContatoPorPessoa(UUID pessoaId, UUID escolaId) {
            calls++;
            return Optional.of(contato);
        }
    }

    private static class FailingContatoPort implements PessoaContatoPort {

        @Override
        public Optional<PessoaContatoResponse> buscarContatoPorPessoa(UUID pessoaId, UUID escolaId) {
            throw new IllegalStateException("contact read failed");
        }
    }
}

