package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import br.com.escola.peopleservice.application.dto.PeopleLocalPersistenceOperationReport;
import br.com.escola.peopleservice.application.dto.PessoaFuncionarioInternalSummaryResponse;
import br.com.escola.peopleservice.application.port.out.PeopleFuncionarioInternalSummaryPort;
import br.com.escola.peopleservice.infra.config.PeopleLocalPersistenceProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PeopleFuncionarioInternalSummaryServiceTest {

    @Test
    void retornaVazioQuandoGuardBloqueiaLeitura() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        CountingFuncionarioInternalSummaryPort port =
                new CountingFuncionarioInternalSummaryPort(funcionario());
        PeopleFuncionarioInternalSummaryService service =
                new PeopleFuncionarioInternalSummaryService(
                        provider(port),
                        guard(new PeopleLocalPersistenceOperationState(), meterRegistry),
                        meterRegistry);

        Optional<PessoaFuncionarioInternalSummaryResponse> response =
                service.buscarFuncionarioPorId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(port.getByIdCalls).isZero();
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.funcionario.internal.summary.reads",
                "operation", "buscarFuncionarioPorId",
                "result", "fallback_guard_blocked").count()).isEqualTo(1.0d);
    }

    @Test
    void retornaVazioQuandoAdapterNaoExiste() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleFuncionarioInternalSummaryService service =
                new PeopleFuncionarioInternalSummaryService(
                        provider(null),
                        guard(greenState(), meterRegistry),
                        meterRegistry);

        Optional<PessoaFuncionarioInternalSummaryResponse> response =
                service.buscarFuncionarioPorId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.funcionario.internal.summary.reads",
                "operation", "buscarFuncionarioPorId",
                "result", "fallback_adapter_missing").count()).isEqualTo(1.0d);
    }

    @Test
    void consultaAdapterQuandoDisponivel() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaFuncionarioInternalSummaryResponse funcionario = funcionario();
        CountingFuncionarioInternalSummaryPort port =
                new CountingFuncionarioInternalSummaryPort(funcionario);
        PeopleFuncionarioInternalSummaryService service =
                new PeopleFuncionarioInternalSummaryService(
                        provider(port),
                        guard(greenState(), meterRegistry),
                        meterRegistry);

        List<PessoaFuncionarioInternalSummaryResponse> response =
                service.listarFuncionariosAtivosPorEscola(UUID.randomUUID());

        assertThat(response).containsExactly(funcionario);
        assertThat(port.listCalls).isEqualTo(1);
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.funcionario.internal.summary.reads",
                "operation", "listarFuncionariosAtivosPorEscola",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void retornaListaVaziaQuandoAdapterFalha() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleFuncionarioInternalSummaryService service =
                new PeopleFuncionarioInternalSummaryService(
                        provider(new FailingFuncionarioInternalSummaryPort()),
                        guard(greenState(), meterRegistry),
                        meterRegistry);

        List<PessoaFuncionarioInternalSummaryResponse> response =
                service.listarFuncionariosAtivosPorEscola(UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.funcionario.internal.summary.reads",
                "operation", "listarFuncionariosAtivosPorEscola",
                "result", "fallback_error").count()).isEqualTo(1.0d);
    }

    private ObjectProvider<PeopleFuncionarioInternalSummaryPort> provider(PeopleFuncionarioInternalSummaryPort port) {
        return new ObjectProvider<>() {
            @Override
            public PeopleFuncionarioInternalSummaryPort getObject(Object... args) {
                return port;
            }

            @Override
            public PeopleFuncionarioInternalSummaryPort getIfAvailable() {
                return port;
            }

            @Override
            public PeopleFuncionarioInternalSummaryPort getIfUnique() {
                return port;
            }

            @Override
            public PeopleFuncionarioInternalSummaryPort getObject() {
                return port;
            }
        };
    }

    private PessoaFuncionarioInternalSummaryResponse funcionario() {
        return new PessoaFuncionarioInternalSummaryResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Maria das Dores",
                "Coordenadora",
                true);
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

    private static class CountingFuncionarioInternalSummaryPort implements PeopleFuncionarioInternalSummaryPort {

        private final PessoaFuncionarioInternalSummaryResponse funcionario;
        private int getByIdCalls;
        private int listCalls;

        private CountingFuncionarioInternalSummaryPort(PessoaFuncionarioInternalSummaryResponse funcionario) {
            this.funcionario = funcionario;
        }

        @Override
        public Optional<PessoaFuncionarioInternalSummaryResponse> buscarFuncionarioPorId(
                UUID funcionarioId,
                UUID escolaId) {
            getByIdCalls++;
            return Optional.of(funcionario);
        }

        @Override
        public List<PessoaFuncionarioInternalSummaryResponse> listarFuncionariosAtivosPorEscola(UUID escolaId) {
            listCalls++;
            return List.of(funcionario);
        }
    }

    private static class FailingFuncionarioInternalSummaryPort implements PeopleFuncionarioInternalSummaryPort {

        @Override
        public Optional<PessoaFuncionarioInternalSummaryResponse> buscarFuncionarioPorId(
                UUID funcionarioId,
                UUID escolaId) {
            throw new IllegalStateException("funcionario read failed");
        }

        @Override
        public List<PessoaFuncionarioInternalSummaryResponse> listarFuncionariosAtivosPorEscola(UUID escolaId) {
            throw new IllegalStateException("funcionario read failed");
        }
    }
}
