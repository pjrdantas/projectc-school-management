package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.PessoaResponsavelVinculadoResponse;
import br.com.escola.peopleservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.peopleservice.application.model.AlunoConsulta;

class ConsultaAlunoServiceTest {

    @Test
    void deveListarAlunosDaEscolaDoContexto() {
        UUID escolaId = UUID.randomUUID();
        AtomicReference<String> nomeRecebido = new AtomicReference<>();
        AtomicReference<UUID> escolaRecebida = new AtomicReference<>();
        ConsultaAlunoService service = new ConsultaAlunoService(
                new br.com.escola.peopleservice.application.port.out.AlunoLeituraPort() {
                    @Override
                    public List<AlunoConsulta> listar(String nome, UUID escola) {
                        nomeRecebido.set(nome);
                        escolaRecebida.set(escola);
                        return List.of(aluno(escola));
                    }

                    @Override
                    public java.util.Optional<AlunoConsulta> buscar(UUID alunoId, UUID escola) {
                        return java.util.Optional.empty();
                    }
                },
                (alunoId, authorization, context) -> List.of());

        var response = service.listar(" Ana ", context(escolaId));

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().escolaId()).isEqualTo(escolaId);
        assertThat(nomeRecebido.get()).isEqualTo(" Ana ");
        assertThat(escolaRecebida.get()).isEqualTo(escolaId);
    }

    @Test
    void deveComporFichaComResponsaveisDoServicoDono() {
        UUID escolaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        InternalRequestContext context = context(escolaId);
        AtomicReference<String> authorizationRecebida = new AtomicReference<>();
        AtomicReference<InternalRequestContext> contextRecebido = new AtomicReference<>();
        PessoaResponsavelVinculadoResponse responsavel = new PessoaResponsavelVinculadoResponse(
                UUID.randomUUID(), "Responsavel", "12345678901", null, null, null, null,
                null, null, null, null, null, null, "MAE", true, true, true, LocalDateTime.now());
        ConsultaAlunoService service = new ConsultaAlunoService(
                new br.com.escola.peopleservice.application.port.out.AlunoLeituraPort() {
                    @Override
                    public List<AlunoConsulta> listar(String nome, UUID escola) {
                        return List.of();
                    }

                    @Override
                    public java.util.Optional<AlunoConsulta> buscar(UUID id, UUID escola) {
                        return java.util.Optional.of(aluno(escola));
                    }
                },
                (id, authorization, receivedContext) -> {
                    authorizationRecebida.set(authorization);
                    contextRecebido.set(receivedContext);
                    return List.of(responsavel);
                });

        var ficha = service.buscarFicha(alunoId, "Bearer access-token", context);

        assertThat(ficha.aluno().escolaId()).isEqualTo(escolaId);
        assertThat(ficha.responsaveis()).containsExactly(responsavel);
        assertThat(authorizationRecebida.get()).isEqualTo("Bearer access-token");
        assertThat(contextRecebido.get()).isEqualTo(context);
    }

    @Test
    void deveRetornarNaoEncontradoQuandoAlunoNaoEstiverAtivoNaEscola() {
        ConsultaAlunoService service = new ConsultaAlunoService(
                new br.com.escola.peopleservice.application.port.out.AlunoLeituraPort() {
                    @Override
                    public List<AlunoConsulta> listar(String nome, UUID escola) {
                        return List.of();
                    }

                    @Override
                    public java.util.Optional<AlunoConsulta> buscar(UUID alunoId, UUID escola) {
                        return java.util.Optional.empty();
                    }
                },
                (alunoId, authorization, context) -> List.of());

        assertThatThrownBy(() -> service.buscar(UUID.randomUUID(), context(UUID.randomUUID())))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    private InternalRequestContext context(UUID escolaId) {
        return new InternalRequestContext("corr-aluno-read", UUID.randomUUID(), escolaId);
    }

    private AlunoConsulta aluno(UUID escolaId) {
        return new AlunoConsulta(
                UUID.randomUUID(), "Ana Aluna", "12345678901", "ana@escola.com", "11999999999",
                LocalDate.of(2015, 3, 10), null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, "ATIVO", escolaId, "Escola B3", LocalDateTime.now());
    }
}
