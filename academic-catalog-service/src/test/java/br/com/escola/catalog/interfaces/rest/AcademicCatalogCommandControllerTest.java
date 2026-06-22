package br.com.escola.catalog.interfaces.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.escola.catalog.application.command.CommandResult;
import br.com.escola.catalog.application.command.CreateDisciplinaCommand;
import br.com.escola.catalog.application.context.InternalHeaders;
import br.com.escola.catalog.application.context.InternalRequestContext;
import br.com.escola.catalog.application.dto.DisciplinaResponse;
import br.com.escola.catalog.application.port.in.CatalogCommandUseCase;
import br.com.escola.catalog.infra.config.InternalApiWebConfiguration;
import br.com.escola.catalog.infra.security.InternalApiInterceptor;
import br.com.escola.catalog.interfaces.advice.CatalogApiExceptionHandler;

@WebMvcTest(AcademicCatalogCommandController.class)
@Import({InternalApiWebConfiguration.class, InternalApiInterceptor.class, CatalogApiExceptionHandler.class})
@TestPropertySource(properties = "catalog.internal-api.token=test-internal-token")
class AcademicCatalogCommandControllerTest {

    private static final UUID USUARIO_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ESCOLA_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogCommandUseCase commandUseCase;

    @Test
    void deveExigirIdempotencyKey() throws Exception {
        mockMvc.perform(authenticatedPost().content("{\"nome\":\"Matematica\",\"cargaHoraria\":80}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void deveCriarComContextoTenantEInformarReplay() throws Exception {
        UUID disciplinaId = UUID.randomUUID();
        when(commandUseCase.criarDisciplina(any(), eq("command-key"), any()))
                .thenReturn(new CommandResult<>(new DisciplinaResponse(
                        disciplinaId, "Matematica", 80, true, ESCOLA_ID, LocalDateTime.now()), true));

        mockMvc.perform(authenticatedPost()
                        .header(InternalHeaders.IDEMPOTENCY_KEY, "command-key")
                        .content("{\"nome\":\"Matematica\",\"cargaHoraria\":80}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(InternalHeaders.IDEMPOTENCY_REPLAYED, "true"))
                .andExpect(jsonPath("$.id").value(disciplinaId.toString()));

        verify(commandUseCase).criarDisciplina(
                eq(new CreateDisciplinaCommand("Matematica", 80)),
                eq("command-key"),
                any(InternalRequestContext.class));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authenticatedPost() {
        return post("/internal/v1/disciplinas")
                .contentType(MediaType.APPLICATION_JSON)
                .header(InternalHeaders.INTERNAL_TOKEN, "test-internal-token")
                .header(InternalHeaders.CORRELATION_ID, "corr-command")
                .header(InternalHeaders.USUARIO_ID, USUARIO_ID)
                .header(InternalHeaders.ESCOLA_ID, ESCOLA_ID);
    }
}
