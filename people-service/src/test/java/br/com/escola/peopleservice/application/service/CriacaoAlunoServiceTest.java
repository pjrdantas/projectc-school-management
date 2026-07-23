package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.AlunoCriacaoRequest;
import br.com.escola.peopleservice.application.exception.InvalidRequestContextException;
import br.com.escola.peopleservice.application.model.AlunoCriado;
import br.com.escola.peopleservice.application.model.EscolaPessoa;

class CriacaoAlunoServiceTest {

    @Test
    void deveNormalizarEUsarEscolaDoContexto() {
        UUID escolaId = UUID.randomUUID();
        AtomicReference<br.com.escola.peopleservice.application.model.AlunoNovo> persisted =
                new AtomicReference<>();
        CriacaoAlunoService service = new CriacaoAlunoService(
                aluno -> {
                    persisted.set(aluno);
                    return criado(aluno.escolaId(), aluno.escolaNome());
                },
                (id, authorization, context) -> new EscolaPessoa(id, "Escola Contexto", true));

        AlunoCriado result = service.criar(
                request(null),
                "Bearer token",
                new InternalRequestContext("corr-create", UUID.randomUUID(), escolaId));

        assertThat(result.escolaId()).isEqualTo(escolaId);
        assertThat(persisted.get().email()).isEqualTo("aluno@escola.com");
        assertThat(persisted.get().uf()).isEqualTo("SP");
        assertThat(persisted.get().statusAluno()).isEqualTo("ATIVO");
    }

    @Test
    void deveBloquearEscolaDiferenteDoContexto() {
        CriacaoAlunoService service = new CriacaoAlunoService(
                aluno -> criado(aluno.escolaId(), aluno.escolaNome()),
                (id, authorization, context) -> new EscolaPessoa(id, "Escola", true));

        assertThatThrownBy(() -> service.criar(
                request(UUID.randomUUID()),
                "Bearer token",
                new InternalRequestContext("corr-create", UUID.randomUUID(), UUID.randomUUID())))
                .isInstanceOf(InvalidRequestContextException.class);
    }

    private AlunoCriacaoRequest request(UUID escolaId) {
        return new AlunoCriacaoRequest(
                " Aluno B3 ", "12345678901", "ALUNO@ESCOLA.COM", "11999999999",
                LocalDate.of(2015, 3, 10), null, null, null, null, null, null, null,
                "01001000", "Praca da Se", "10", null, "Se", "Sao Paulo", "sp", null, escolaId);
    }

    private AlunoCriado criado(UUID escolaId, String escolaNome) {
        return new AlunoCriado(
                UUID.randomUUID(), "Aluno B3", "12345678901", "aluno@escola.com",
                "11999999999", LocalDate.of(2015, 3, 10), null, null, null, null,
                null, null, null, "01001000", "Praca da Se", "10", null, "Se",
                "Sao Paulo", "SP", "ATIVO", escolaId, escolaNome, LocalDateTime.now());
    }
}
