package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.AlunoAtualizacaoRequest;
import br.com.escola.peopleservice.application.exception.InvalidRequestContextException;
import br.com.escola.peopleservice.application.model.AlunoAlteracao;
import br.com.escola.peopleservice.application.model.AlunoAtualizado;

class AtualizacaoAlunoServiceTest {

    @Test
    void deveNormalizarDadosEUsarEscolaDoContexto() {
        UUID escolaId = UUID.randomUUID();
        AtomicReference<AlunoAlteracao> persisted = new AtomicReference<>();
        AtualizacaoAlunoService service = new AtualizacaoAlunoService((alunoId, aluno) -> {
            persisted.set(aluno);
            return atualizado(alunoId, aluno);
        });
        UUID alunoId = UUID.randomUUID();

        AlunoAtualizado result = service.atualizar(
                alunoId,
                request(null, "01001000", "10"),
                new InternalRequestContext("corr-update", UUID.randomUUID(), escolaId));

        assertThat(result.id()).isEqualTo(alunoId);
        assertThat(persisted.get().escolaId()).isEqualTo(escolaId);
        assertThat(persisted.get().nomeCompleto()).isEqualTo("Aluno Atualizado");
        assertThat(persisted.get().email()).isEqualTo("aluno@escola.com");
        assertThat(persisted.get().uf()).isEqualTo("SP");
        assertThat(persisted.get().statusAluno()).isEqualTo("ATIVO");
    }

    @Test
    void deveBloquearEscolaDiferenteDoContexto() {
        AtualizacaoAlunoService service = new AtualizacaoAlunoService(this::atualizado);

        assertThatThrownBy(() -> service.atualizar(
                UUID.randomUUID(),
                request(UUID.randomUUID(), null, null),
                new InternalRequestContext("corr-update", UUID.randomUUID(), UUID.randomUUID())))
                .isInstanceOf(InvalidRequestContextException.class);
    }

    @Test
    void deveExigirNumeroQuandoCepForInformado() {
        AtualizacaoAlunoService service = new AtualizacaoAlunoService(this::atualizado);
        UUID escolaId = UUID.randomUUID();

        assertThatThrownBy(() -> service.atualizar(
                UUID.randomUUID(),
                request(escolaId, "01001000", null),
                new InternalRequestContext("corr-update", UUID.randomUUID(), escolaId)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("numero");
    }

    private AlunoAtualizacaoRequest request(UUID escolaId, String cep, String numero) {
        return new AlunoAtualizacaoRequest(
                " Aluno Atualizado ", "12345678901", "ALUNO@ESCOLA.COM", "11999999999",
                LocalDate.of(2014, 4, 11), null, null, null, null, null, null, null,
                cep, "Praca da Se", numero, null, "Se", "Sao Paulo", "sp", null, escolaId);
    }

    private AlunoAtualizado atualizado(UUID alunoId, AlunoAlteracao aluno) {
        return new AlunoAtualizado(
                alunoId, aluno.nomeCompleto(), aluno.cpf(), aluno.email(), aluno.telefone(),
                aluno.dataNascimento(), aluno.rg(), aluno.orgaoEmissorRg(), aluno.ufRg(),
                aluno.nacionalidade(), aluno.naturalidade(), aluno.sexo(), aluno.nomeSocial(),
                aluno.cep(), aluno.logradouro(), aluno.numero(), aluno.complemento(), aluno.bairro(),
                aluno.cidade(), aluno.uf(), aluno.statusAluno(), aluno.escolaId(), "Escola B3",
                LocalDateTime.now());
    }
}
