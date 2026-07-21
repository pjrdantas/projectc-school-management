package br.com.escola.peopleservice.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.com.escola.peopleservice.application.context.InternalHeaders;
import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.port.in.ExcluirAlunoUseCase;
import br.com.escola.peopleservice.interfaces.advice.ApiExceptionHandler;

class AlunoExclusaoControllerTest {

    @Test
    void deveExporExclusaoInternaComRespostaSemConteudo() throws Exception {
        UUID alunoId = UUID.randomUUID();
        InternalRequestContext context = new InternalRequestContext(
                "corr-aluno-delete", UUID.randomUUID(), UUID.randomUUID());
        AtomicReference<InternalRequestContext> receivedContext = new AtomicReference<>();
        ExcluirAlunoUseCase useCase = (id, internalContext) -> receivedContext.set(internalContext);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AlunoExclusaoController(useCase))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        mockMvc.perform(delete("/internal/v1/alunos/{alunoId}", alunoId)
                        .requestAttr(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE, context))
                .andExpect(status().isNoContent());

        assertThat(receivedContext.get()).isEqualTo(context);
    }
}
