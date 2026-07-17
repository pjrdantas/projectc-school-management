package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import br.com.escola.peopleservice.application.state.LeituraModeloSyncSummary;
import br.com.escola.peopleservice.application.dto.PessoaFuncionarioResumoResponse;
import br.com.escola.peopleservice.application.port.out.PessoaFuncionarioResumoPort;
import br.com.escola.peopleservice.infra.config.LeituraModeloProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PessoaFuncionarioResumoServiceTest {

    @Test
    void retornaVazioQuandoGuardBloqueiaLeitura() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        CountingFuncionarioResumoPort port =
                new CountingFuncionarioResumoPort(funcionario());
        PessoaFuncionarioResumoService service =
                new PessoaFuncionarioResumoService(
                        provider(port),
                        guard(new LeituraModeloSyncState(), meterRegistry),
                        meterRegistry);

        Optional<PessoaFuncionarioResumoResponse> response =
                service.buscarFuncionarioPorId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(port.getByIdCalls).isZero();
        assertThat(meterRegistry.counter(
                "people.funcionario.reads",
                "operation", "buscarFuncionarioPorId",
                "result", "fallback_guard_blocked").count()).isEqualTo(1.0d);
    }

    @Test
    void retornaVazioQuandoAdapterNaoExiste() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaFuncionarioResumoService service =
                new PessoaFuncionarioResumoService(
                        provider(null),
                        guard(greenState(), meterRegistry),
                        meterRegistry);

        Optional<PessoaFuncionarioResumoResponse> response =
                service.buscarFuncionarioPorId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.funcionario.reads",
                "operation", "buscarFuncionarioPorId",
                "result", "fallback_adapter_missing").count()).isEqualTo(1.0d);
    }

    @Test
    void consultaAdapterQuandoDisponivel() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaFuncionarioResumoResponse funcionario = funcionario();
        CountingFuncionarioResumoPort port =
                new CountingFuncionarioResumoPort(funcionario);
        PessoaFuncionarioResumoService service =
                new PessoaFuncionarioResumoService(
                        provider(port),
                        guard(greenState(), meterRegistry),
                        meterRegistry);

        List<PessoaFuncionarioResumoResponse> response =
                service.listarFuncionariosAtivosPorEscola(UUID.randomUUID());

        assertThat(response).containsExactly(funcionario);
        assertThat(port.listCalls).isEqualTo(1);
        assertThat(meterRegistry.counter(
                "people.funcionario.reads",
                "operation", "listarFuncionariosAtivosPorEscola",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void retornaListaVaziaQuandoAdapterFalha() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaFuncionarioResumoService service =
                new PessoaFuncionarioResumoService(
                        provider(new FailingFuncionarioResumoPort()),
                        guard(greenState(), meterRegistry),
                        meterRegistry);

        List<PessoaFuncionarioResumoResponse> response =
                service.listarFuncionariosAtivosPorEscola(UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.funcionario.reads",
                "operation", "listarFuncionariosAtivosPorEscola",
                "result", "fallback_error").count()).isEqualTo(1.0d);
    }

    private ObjectProvider<PessoaFuncionarioResumoPort> provider(PessoaFuncionarioResumoPort port) {
        return new ObjectProvider<>() {
            @Override
            public PessoaFuncionarioResumoPort getObject(Object... args) {
                return port;
            }

            @Override
            public PessoaFuncionarioResumoPort getIfAvailable() {
                return port;
            }

            @Override
            public PessoaFuncionarioResumoPort getIfUnique() {
                return port;
            }

            @Override
            public PessoaFuncionarioResumoPort getObject() {
                return port;
            }
        };
    }

    private PessoaFuncionarioResumoResponse funcionario() {
        return new PessoaFuncionarioResumoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Maria das Dores",
                "Coordenadora",
                true);
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
                11,
                11,
                22,
                22,
                22,
                0,
                false,
                false,
                List.of()));
        return state;
    }

    private static class CountingFuncionarioResumoPort implements PessoaFuncionarioResumoPort {

        private final PessoaFuncionarioResumoResponse funcionario;
        private int getByIdCalls;
        private int listCalls;

        private CountingFuncionarioResumoPort(PessoaFuncionarioResumoResponse funcionario) {
            this.funcionario = funcionario;
        }

        @Override
        public Optional<PessoaFuncionarioResumoResponse> buscarFuncionarioPorId(
                UUID funcionarioId,
                UUID escolaId) {
            getByIdCalls++;
            return Optional.of(funcionario);
        }

        @Override
        public List<PessoaFuncionarioResumoResponse> listarFuncionariosAtivosPorEscola(UUID escolaId) {
            listCalls++;
            return List.of(funcionario);
        }
    }

    private static class FailingFuncionarioResumoPort implements PessoaFuncionarioResumoPort {

        @Override
        public Optional<PessoaFuncionarioResumoResponse> buscarFuncionarioPorId(
                UUID funcionarioId,
                UUID escolaId) {
            throw new IllegalStateException("funcionario read failed");
        }

        @Override
        public List<PessoaFuncionarioResumoResponse> listarFuncionariosAtivosPorEscola(UUID escolaId) {
            throw new IllegalStateException("funcionario read failed");
        }
    }
}



