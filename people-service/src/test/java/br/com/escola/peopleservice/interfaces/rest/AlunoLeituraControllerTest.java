package br.com.escola.peopleservice.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.com.escola.peopleservice.application.context.InternalHeaders;
import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.AlunoFichaResponse;
import br.com.escola.peopleservice.application.dto.AlunoResponse;
import br.com.escola.peopleservice.application.dto.PessoaResponsavelVinculadoResponse;
import br.com.escola.peopleservice.application.port.in.ConsultarAlunoUseCase;
import br.com.escola.peopleservice.interfaces.advice.ApiExceptionHandler;

class AlunoLeituraControllerTest {

    @Test
    void deveExporListagemDetalheEFichaInternos() throws Exception {
        UUID alunoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        InternalRequestContext context = new InternalRequestContext(
                "corr-aluno-read", UUID.randomUUID(), escolaId);
        AtomicReference<String> nomeRecebido = new AtomicReference<>();
        AlunoResponse aluno = aluno(alunoId, escolaId);
        ConsultarAlunoUseCase useCase = new ConsultarAlunoUseCase() {
            @Override
            public List<AlunoResponse> listar(String nome, InternalRequestContext internalContext) {
                nomeRecebido.set(nome);
                return List.of(aluno);
            }

            @Override
            public AlunoResponse buscar(UUID id, InternalRequestContext internalContext) {
                return aluno;
            }

            @Override
            public AlunoFichaResponse buscarFicha(
                    UUID id,
                    String authorization,
                    InternalRequestContext internalContext) {
                return new AlunoFichaResponse(aluno, List.of(new PessoaResponsavelVinculadoResponse(
                        UUID.randomUUID(), "Responsavel", "12345678901", null, null, null, null,
                        null, null, null, null, null, null, "MAE", true, true, true, LocalDateTime.now())));
            }
        };
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AlunoLeituraController(useCase))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        mockMvc.perform(get("/internal/v1/alunos").param("nome", "Ana")
                        .requestAttr(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE, context))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(alunoId.toString()));
        mockMvc.perform(get("/internal/v1/alunos/{alunoId}", alunoId)
                        .requestAttr(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE, context))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escolaId").value(escolaId.toString()));
        mockMvc.perform(get("/internal/v1/alunos/{alunoId}/ficha", alunoId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .requestAttr(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE, context))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aluno.id").value(alunoId.toString()))
                .andExpect(jsonPath("$.responsaveis[0].parentesco").value("MAE"));

        assertThat(nomeRecebido.get()).isEqualTo("Ana");
    }

    private AlunoResponse aluno(UUID alunoId, UUID escolaId) {
        return new AlunoResponse(
                alunoId, "Ana Aluna", "12345678901", "ana@escola.com", "11999999999",
                LocalDate.of(2015, 3, 10), null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, "ATIVO", escolaId, "Escola B3", LocalDateTime.now());
    }
}
