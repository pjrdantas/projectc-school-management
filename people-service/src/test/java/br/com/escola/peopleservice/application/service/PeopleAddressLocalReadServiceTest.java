package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.application.dto.PeopleLocalPersistenceOperationReport;
import br.com.escola.peopleservice.application.dto.PessoaEnderecoLocalReadResponse;
import br.com.escola.peopleservice.application.port.out.PeopleAddressLocalReadPort;
import br.com.escola.peopleservice.infra.config.PeopleLocalPersistenceProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PeopleAddressLocalReadServiceTest {

    @Test
    void naoConsultaAdapterQuandoGuardNaoEstaElegivel() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        CountingAddressLocalReadPort port = new CountingAddressLocalReadPort(endereco());
        PeopleAddressLocalReadService service = new PeopleAddressLocalReadService(
                port,
                guard(new PeopleLocalPersistenceOperationState(), meterRegistry),
                meterRegistry);

        Optional<PessoaEnderecoLocalReadResponse> response = service.buscarEnderecoPrincipalPorPessoa(
                UUID.randomUUID(),
                UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(port.principalCalls).isZero();
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.address.reads",
                "operation", "buscarEnderecoPrincipalPorPessoa",
                "result", "fallback_guard_blocked").count()).isEqualTo(1.0d);
    }

    @Test
    void consultaAdapterQuandoGuardEstaElegivel() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaEnderecoLocalReadResponse endereco = endereco();
        CountingAddressLocalReadPort port = new CountingAddressLocalReadPort(endereco);
        PeopleAddressLocalReadService service = new PeopleAddressLocalReadService(
                port,
                guard(greenState(), meterRegistry),
                meterRegistry);

        Optional<PessoaEnderecoLocalReadResponse> response = service.buscarEnderecoPrincipalPorPessoa(
                UUID.randomUUID(),
                UUID.randomUUID());

        assertThat(response).contains(endereco);
        assertThat(port.principalCalls).isEqualTo(1);
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.address.reads",
                "operation", "buscarEnderecoPrincipalPorPessoa",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void registraFallbackQuandoAdapterFalha() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleAddressLocalReadService service = new PeopleAddressLocalReadService(
                new FailingAddressLocalReadPort(),
                guard(greenState(), meterRegistry),
                meterRegistry);

        List<PessoaEnderecoLocalReadResponse> response = service.listarEnderecosPorPessoa(
                UUID.randomUUID(),
                UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.address.reads",
                "operation", "listarEnderecosPorPessoa",
                "result", "fallback_error").count()).isEqualTo(1.0d);
    }

    private PeopleLocalReadCutoverGuard guard(
            PeopleLocalPersistenceOperationState state,
            SimpleMeterRegistry meterRegistry) {
        return new PeopleLocalReadCutoverGuard(
                new PeopleLocalPersistenceProperties(true, true, true, false, true, true, 500, true),
                meterRegistry,
                state);
    }

    private PeopleLocalPersistenceOperationState greenState() {
        PeopleLocalPersistenceOperationState state = new PeopleLocalPersistenceOperationState();
        state.update(new PeopleLocalPersistenceOperationReport(
                true,
                true,
                "completed",
                "local-read-model-backfill-and-reconciliation-completed",
                500,
                9,
                9,
                25,
                25,
                25,
                0,
                false,
                false,
                List.of()));
        return state;
    }

    private PessoaEnderecoLocalReadResponse endereco() {
        UUID pessoaId = UUID.randomUUID();
        return new PessoaEnderecoLocalReadResponse(
                UUID.randomUUID(),
                pessoaId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "RESIDENCIAL",
                "Residencial",
                true,
                "01001000",
                "Praca da Se",
                "100",
                null,
                "Se",
                "Sao Paulo",
                "SP");
    }

    private static class CountingAddressLocalReadPort implements PeopleAddressLocalReadPort {

        private final PessoaEnderecoLocalReadResponse endereco;
        private int principalCalls;

        private CountingAddressLocalReadPort(PessoaEnderecoLocalReadResponse endereco) {
            this.endereco = endereco;
        }

        @Override
        public Optional<PessoaEnderecoLocalReadResponse> buscarEnderecoPrincipalPorPessoa(
                UUID pessoaId,
                UUID escolaId) {
            principalCalls++;
            return Optional.of(endereco);
        }

        @Override
        public List<PessoaEnderecoLocalReadResponse> listarEnderecosPorPessoa(UUID pessoaId, UUID escolaId) {
            return List.of(endereco);
        }
    }

    private static class FailingAddressLocalReadPort implements PeopleAddressLocalReadPort {

        @Override
        public Optional<PessoaEnderecoLocalReadResponse> buscarEnderecoPrincipalPorPessoa(
                UUID pessoaId,
                UUID escolaId) {
            throw new IllegalStateException("address read failed");
        }

        @Override
        public List<PessoaEnderecoLocalReadResponse> listarEnderecosPorPessoa(UUID pessoaId, UUID escolaId) {
            throw new IllegalStateException("address read failed");
        }
    }
}
