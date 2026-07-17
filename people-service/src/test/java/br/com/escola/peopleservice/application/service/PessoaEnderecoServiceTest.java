package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.application.state.LeituraModeloSyncSummary;
import br.com.escola.peopleservice.application.dto.PessoaEnderecoResponse;
import br.com.escola.peopleservice.application.port.out.PessoaEnderecoPort;
import br.com.escola.peopleservice.infra.config.LeituraModeloProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PessoaEnderecoServiceTest {

    @Test
    void naoConsultaAdapterQuandoGuardNaoEstaElegivel() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        CountingEnderecoPort port = new CountingEnderecoPort(endereco());
        PessoaEnderecoService service = new PessoaEnderecoService(
                port,
                guard(new LeituraModeloSyncState(), meterRegistry),
                meterRegistry);

        Optional<PessoaEnderecoResponse> response = service.buscarEnderecoPrincipalPorPessoa(
                UUID.randomUUID(),
                UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(port.principalCalls).isZero();
        assertThat(meterRegistry.counter(
                "people.address.reads",
                "operation", "buscarEnderecoPrincipalPorPessoa",
                "result", "fallback_guard_blocked").count()).isEqualTo(1.0d);
    }

    @Test
    void consultaAdapterQuandoGuardEstaElegivel() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaEnderecoResponse endereco = endereco();
        CountingEnderecoPort port = new CountingEnderecoPort(endereco);
        PessoaEnderecoService service = new PessoaEnderecoService(
                port,
                guard(greenState(), meterRegistry),
                meterRegistry);

        Optional<PessoaEnderecoResponse> response = service.buscarEnderecoPrincipalPorPessoa(
                UUID.randomUUID(),
                UUID.randomUUID());

        assertThat(response).contains(endereco);
        assertThat(port.principalCalls).isEqualTo(1);
        assertThat(meterRegistry.counter(
                "people.address.reads",
                "operation", "buscarEnderecoPrincipalPorPessoa",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void registraFallbackQuandoAdapterFalha() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaEnderecoService service = new PessoaEnderecoService(
                new FailingEnderecoPort(),
                guard(greenState(), meterRegistry),
                meterRegistry);

        List<PessoaEnderecoResponse> response = service.listarEnderecosPorPessoa(
                UUID.randomUUID(),
                UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.address.reads",
                "operation", "listarEnderecosPorPessoa",
                "result", "fallback_error").count()).isEqualTo(1.0d);
    }

    private OrigemLeituraPolicy guard(
            LeituraModeloSyncState state,
            SimpleMeterRegistry meterRegistry) {
        return new OrigemLeituraPolicy(
                new LeituraModeloProperties(true, true, true, false, true, true, 500, true),
                meterRegistry,
                state);
    }

    private LeituraModeloSyncState greenState() {
        LeituraModeloSyncState state = new LeituraModeloSyncState();
        state.update(new LeituraModeloSyncSummary(
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

    private PessoaEnderecoResponse endereco() {
        UUID pessoaId = UUID.randomUUID();
        return new PessoaEnderecoResponse(
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

    private static class CountingEnderecoPort implements PessoaEnderecoPort {

        private final PessoaEnderecoResponse endereco;
        private int principalCalls;

        private CountingEnderecoPort(PessoaEnderecoResponse endereco) {
            this.endereco = endereco;
        }

        @Override
        public Optional<PessoaEnderecoResponse> buscarEnderecoPrincipalPorPessoa(
                UUID pessoaId,
                UUID escolaId) {
            principalCalls++;
            return Optional.of(endereco);
        }

        @Override
        public List<PessoaEnderecoResponse> listarEnderecosPorPessoa(UUID pessoaId, UUID escolaId) {
            return List.of(endereco);
        }
    }

    private static class FailingEnderecoPort implements PessoaEnderecoPort {

        @Override
        public Optional<PessoaEnderecoResponse> buscarEnderecoPrincipalPorPessoa(
                UUID pessoaId,
                UUID escolaId) {
            throw new IllegalStateException("address read failed");
        }

        @Override
        public List<PessoaEnderecoResponse> listarEnderecosPorPessoa(UUID pessoaId, UUID escolaId) {
            throw new IllegalStateException("address read failed");
        }
    }
}



