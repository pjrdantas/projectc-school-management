package br.com.escola.catalog.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.escola.catalog.application.context.InternalHeaders;
import br.com.escola.catalog.application.context.InternalRequestContext;
import br.com.escola.catalog.application.dto.DisciplinaResponse;
import br.com.escola.catalog.application.port.in.CatalogQueryUseCase;
import br.com.escola.catalog.infra.config.InternalApiWebConfiguration;
import br.com.escola.catalog.infra.security.InternalApiInterceptor;
import br.com.escola.catalog.interfaces.advice.CatalogApiExceptionHandler;

@WebMvcTest(AcademicCatalogQueryController.class)
@Import({InternalApiWebConfiguration.class, InternalApiInterceptor.class, CatalogApiExceptionHandler.class})
@TestPropertySource(properties = "catalog.internal-api.token=test-internal-token")
class AcademicCatalogQueryControllerTest {

    private static final UUID USUARIO_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ESCOLA_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogQueryUseCase catalogQueryUseCase;

    @Test
    void deveExigirCredencialInterna() throws Exception {
        mockMvc.perform(get("/internal/v1/disciplinas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INTERNAL_UNAUTHORIZED"));
    }

    @Test
    void deveRejeitarContextoTenantIncompleto() throws Exception {
        mockMvc.perform(get("/internal/v1/disciplinas")
                        .header(InternalHeaders.INTERNAL_TOKEN, "test-internal-token")
                        .header(InternalHeaders.CORRELATION_ID, "corr-51d")
                        .header(InternalHeaders.USUARIO_ID, USUARIO_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_CONTEXT"));
    }

    @Test
    void devePropagarContextoValidadoAoCasoDeUso() throws Exception {
        UUID disciplinaId = UUID.randomUUID();
        when(catalogQueryUseCase.listarDisciplinas(any())).thenReturn(List.of(new DisciplinaResponse(
                disciplinaId, "Matematica", 80, true, ESCOLA_ID, LocalDateTime.now())));

        mockMvc.perform(authenticatedGet("/internal/v1/disciplinas"))
                .andExpect(status().isOk())
                .andExpect(header().string(InternalHeaders.CORRELATION_ID, "corr-51d"))
                .andExpect(jsonPath("$[0].id").value(disciplinaId.toString()))
                .andExpect(jsonPath("$[0].escolaId").value(ESCOLA_ID.toString()));

        ArgumentCaptor<InternalRequestContext> captor = ArgumentCaptor.forClass(InternalRequestContext.class);
        verify(catalogQueryUseCase).listarDisciplinas(captor.capture());
        assertThat(captor.getValue().usuarioId()).isEqualTo(USUARIO_ID);
        assertThat(captor.getValue().escolaId().value()).isEqualTo(ESCOLA_ID);
        assertThat(captor.getValue().correlationId()).isEqualTo("corr-51d");
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authenticatedGet(String path) {
        return get(path)
                .header(InternalHeaders.INTERNAL_TOKEN, "test-internal-token")
                .header(InternalHeaders.CORRELATION_ID, "corr-51d")
                .header(InternalHeaders.USUARIO_ID, USUARIO_ID)
                .header(InternalHeaders.ESCOLA_ID, ESCOLA_ID);
    }
}
