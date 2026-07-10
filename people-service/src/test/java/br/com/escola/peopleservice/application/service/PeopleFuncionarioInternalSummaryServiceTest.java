package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import br.com.escola.peopleservice.application.dto.PessoaFuncionarioInternalSummaryResponse;
import br.com.escola.peopleservice.application.port.out.PeopleFuncionarioInternalSummaryPort;

class PeopleFuncionarioInternalSummaryServiceTest {

    @Test
    void retornaVazioQuandoAdapterNaoExiste() {
        PeopleFuncionarioInternalSummaryService service =
                new PeopleFuncionarioInternalSummaryService(provider(null));

        Optional<PessoaFuncionarioInternalSummaryResponse> response =
                service.buscarFuncionarioPorId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
    }

    @Test
    void consultaAdapterQuandoDisponivel() {
        PessoaFuncionarioInternalSummaryResponse funcionario = funcionario();
        CountingFuncionarioInternalSummaryPort port =
                new CountingFuncionarioInternalSummaryPort(funcionario);
        PeopleFuncionarioInternalSummaryService service =
                new PeopleFuncionarioInternalSummaryService(provider(port));

        List<PessoaFuncionarioInternalSummaryResponse> response =
                service.listarFuncionariosAtivosPorEscola(UUID.randomUUID());

        assertThat(response).containsExactly(funcionario);
        assertThat(port.listCalls).isEqualTo(1);
    }

    @Test
    void retornaListaVaziaQuandoAdapterFalha() {
        PeopleFuncionarioInternalSummaryService service =
                new PeopleFuncionarioInternalSummaryService(provider(new FailingFuncionarioInternalSummaryPort()));

        List<PessoaFuncionarioInternalSummaryResponse> response =
                service.listarFuncionariosAtivosPorEscola(UUID.randomUUID());

        assertThat(response).isEmpty();
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

    private static class CountingFuncionarioInternalSummaryPort implements PeopleFuncionarioInternalSummaryPort {

        private final PessoaFuncionarioInternalSummaryResponse funcionario;
        private int listCalls;

        private CountingFuncionarioInternalSummaryPort(PessoaFuncionarioInternalSummaryResponse funcionario) {
            this.funcionario = funcionario;
        }

        @Override
        public Optional<PessoaFuncionarioInternalSummaryResponse> buscarFuncionarioPorId(
                UUID funcionarioId,
                UUID escolaId) {
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
