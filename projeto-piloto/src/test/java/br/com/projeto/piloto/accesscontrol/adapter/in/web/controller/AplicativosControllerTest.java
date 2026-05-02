
package br.com.projeto.piloto.accesscontrol.adapter.in.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.projeto.piloto.aplicativos.adapter.in.web.controller.AplicativosController;
import br.com.projeto.piloto.aplicativos.adapter.in.web.dto.AplicativosRequestDTO;
import br.com.projeto.piloto.aplicativos.application.port.in.AplicativosUseCase;
import br.com.projeto.piloto.aplicativos.domain.model.AplicativosModel;

@WebMvcTest(AplicativosController.class)
@ContextConfiguration(classes = AplicativosController.class)
@AutoConfigureMockMvc(addFilters = false)
class AplicativosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
	@MockBean
    private AplicativosUseCase aplicativosUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AplicativosRequestDTO validDto;
    private AplicativosModel validModel;

    @BeforeEach
    void setup() {
        validDto = new AplicativosRequestDTO(
                "App Teste",
                "Descricao Teste",
                "http://localhost",
                "appModule",
                "AppComponent",
                "/app",
                "S"
        );

        validModel = AplicativosModel.builder()
                .id(1L)
                .nmAplicativo("App Teste")
                .dsAplicativo("Descricao Teste")
                .dsUrl("http://localhost")
                .nmModulo("appModule")
                .moduloExposto("AppComponent")
                .dsRota("/app")
                .flAtivo("S")
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST - Criar aplicativo com sucesso (201)")
    void createSuccess() throws Exception {
        when(aplicativosUseCase.existsByNome("App Teste")).thenReturn(false);
        when(aplicativosUseCase.create(any())).thenReturn(validModel);

        mockMvc.perform(post("/api/aplicativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("App Teste"));
    }

    @Test
    @DisplayName("POST - Criar aplicativo com nome já existente (409)")
    void createConflict() throws Exception {
        when(aplicativosUseCase.existsByNome("App Teste")).thenReturn(true);

        mockMvc.perform(post("/api/aplicativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT - Atualizar aplicativo existente (200)")
    void updateSuccess() throws Exception {
        when(aplicativosUseCase.findById(1L)).thenReturn(Optional.of(validModel));
        when(aplicativosUseCase.update(eq(1L), any())).thenReturn(validModel);

        mockMvc.perform(put("/api/aplicativos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("App Teste"));
    }

    @Test
    @DisplayName("PUT - Atualizar aplicativo não encontrado (404)")
    void updateNotFound() throws Exception {
        when(aplicativosUseCase.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/aplicativos/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE - Remover aplicativo existente (204)")
    void deleteSuccess() throws Exception {
        when(aplicativosUseCase.findById(1L)).thenReturn(Optional.of(validModel));
        doNothing().when(aplicativosUseCase).delete(1L);

        mockMvc.perform(delete("/api/aplicativos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE - Remover aplicativo não encontrado (404)")
    void deleteNotFound() throws Exception {
        when(aplicativosUseCase.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/aplicativos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /{id} - Buscar aplicativo por id existente (200)")
    void findByIdSuccess() throws Exception {
        when(aplicativosUseCase.findById(1L)).thenReturn(Optional.of(validModel));

        mockMvc.perform(get("/api/aplicativos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /{id} - Buscar aplicativo por id não encontrado (404)")
    void findByIdNotFound() throws Exception {
        when(aplicativosUseCase.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/aplicativos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET - Listar todos os aplicativos com registros (200)")
    void listAllSuccess() throws Exception {
        when(aplicativosUseCase.listAll()).thenReturn(List.of(validModel));

        mockMvc.perform(get("/api/aplicativos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("App Teste"));
    }

    @Test
    @DisplayName("GET - Listar todos os aplicativos com lista vazia (200)")
    void listAllEmpty() throws Exception {
        when(aplicativosUseCase.listAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/aplicativos"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /ativos - Listar aplicativos ativos com registros (200)")
    void listAtivosSuccess() throws Exception {
        when(aplicativosUseCase.listAtivos()).thenReturn(List.of(validModel));

        mockMvc.perform(get("/api/aplicativos/ativos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("App Teste"));
    }

    @Test
    @DisplayName("GET /ativos - Listar aplicativos ativos com lista vazia (200)")
    void listAtivosEmpty() throws Exception {
        when(aplicativosUseCase.listAtivos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/aplicativos/ativos"))
                .andExpect(status().isOk());
    }
}
