package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import br.com.escola.peopleservice.application.dto.PessoaContatoLocalReadResponse;
import br.com.escola.peopleservice.application.port.out.PeopleContactLocalReadPort;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PeopleContactLocalReadServiceTest {

    @Test
    void retornaVazioQuandoAdapterNaoExiste() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleContactLocalReadService service = new PeopleContactLocalReadService(provider(null), meterRegistry);

        Optional<PessoaContatoLocalReadResponse> response =
                service.buscarContatoPorPessoa(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.contact.reads",
                "operation", "buscarContatoPorPessoa",
                "result", "fallback_adapter_missing").count()).isEqualTo(1.0d);
    }

    @Test
    void consultaAdapterQuandoDisponivel() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaContatoLocalReadResponse contato = contato();
        CountingContactLocalReadPort port = new CountingContactLocalReadPort(contato);
        PeopleContactLocalReadService service = new PeopleContactLocalReadService(provider(port), meterRegistry);

        Optional<PessoaContatoLocalReadResponse> response =
                service.buscarContatoPorPessoa(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).contains(contato);
        assertThat(port.calls).isEqualTo(1);
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.contact.reads",
                "operation", "buscarContatoPorPessoa",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void registraFallbackQuandoAdapterFalha() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleContactLocalReadService service =
                new PeopleContactLocalReadService(provider(new FailingContactLocalReadPort()), meterRegistry);

        Optional<PessoaContatoLocalReadResponse> response =
                service.buscarContatoPorPessoa(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.contact.reads",
                "operation", "buscarContatoPorPessoa",
                "result", "fallback_error").count()).isEqualTo(1.0d);
    }

    private ObjectProvider<PeopleContactLocalReadPort> provider(PeopleContactLocalReadPort port) {
        return new ObjectProvider<>() {
            @Override
            public PeopleContactLocalReadPort getObject(Object... args) {
                return port;
            }

            @Override
            public PeopleContactLocalReadPort getIfAvailable() {
                return port;
            }

            @Override
            public PeopleContactLocalReadPort getIfUnique() {
                return port;
            }

            @Override
            public PeopleContactLocalReadPort getObject() {
                return port;
            }
        };
    }

    private PessoaContatoLocalReadResponse contato() {
        return new PessoaContatoLocalReadResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ana.aluna@example.com",
                "11999999999",
                true);
    }

    private static class CountingContactLocalReadPort implements PeopleContactLocalReadPort {

        private final PessoaContatoLocalReadResponse contato;
        private int calls;

        private CountingContactLocalReadPort(PessoaContatoLocalReadResponse contato) {
            this.contato = contato;
        }

        @Override
        public Optional<PessoaContatoLocalReadResponse> buscarContatoPorPessoa(UUID pessoaId, UUID escolaId) {
            calls++;
            return Optional.of(contato);
        }
    }

    private static class FailingContactLocalReadPort implements PeopleContactLocalReadPort {

        @Override
        public Optional<PessoaContatoLocalReadResponse> buscarContatoPorPessoa(UUID pessoaId, UUID escolaId) {
            throw new IllegalStateException("contact read failed");
        }
    }
}
