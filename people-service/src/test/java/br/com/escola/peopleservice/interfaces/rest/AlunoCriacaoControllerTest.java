package br.com.escola.peopleservice.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.com.escola.peopleservice.application.context.InternalHeaders;
import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.model.AlunoCriado;
import br.com.escola.peopleservice.application.port.in.CriarAlunoUseCase;
import br.com.escola.peopleservice.interfaces.advice.ApiExceptionHandler;

class AlunoCriacaoControllerTest {

    @Test
    void deveExporCriacaoInternaComContratoCompativel() throws Exception {
        UUID alunoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        InternalRequestContext context = new InternalRequestContext(
                "corr-aluno-create", UUID.randomUUID(), escolaId);
        AtomicReference<InternalRequestContext> receivedContext = new AtomicReference<>();
        CriarAlunoUseCase useCase = (request, authorization, internalContext) -> {
            receivedContext.set(internalContext);
            return new AlunoCriado(
                    alunoId, request.nomeCompleto(), request.cpf(), request.email(), request.telefone(),
                    request.dataNascimento(), request.rg(), request.orgaoEmissorRg(), request.ufRg(),
                    request.nacionalidade(), request.naturalidade(), request.sexo(), request.nomeSocial(),
                    request.cep(), request.logradouro(), request.numero(), request.complemento(), request.bairro(),
                    request.cidade(), request.uf(), "ATIVO", escolaId, "Escola B3", LocalDateTime.now());
        };
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AlunoCriacaoController(useCase))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        mockMvc.perform(post("/internal/v1/alunos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .requestAttr(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE, context)
                        .contentType("application/json")
                        .content("""
                                {
                                  "nomeCompleto":"Aluno B3",
                                  "cpf":"12345678901",
                                  "email":"aluno@escola.com",
                                  "telefone":"11999999999",
                                  "dataNascimento":"2015-03-10",
                                  "cep":"01001000",
                                  "numero":"10"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(alunoId.toString()))
                .andExpect(jsonPath("$.escolaId").value(escolaId.toString()))
                .andExpect(jsonPath("$.escolaNome").value("Escola B3"))
                .andExpect(jsonPath("$.statusAluno").value("ATIVO"));

        org.assertj.core.api.Assertions.assertThat(receivedContext.get()).isEqualTo(context);
    }
}
