
package br.com.projeto.piloto.accesscontrol.adapter.in.web.controller;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.projeto.piloto.accesscontrol.adapter.in.web.dto.AuthUsuarioRequestDTO;
import br.com.projeto.piloto.accesscontrol.application.port.in.AuthUsuarioUseCasePort;
import br.com.projeto.piloto.accesscontrol.domain.exception.UserNotFoundException;
import br.com.projeto.piloto.accesscontrol.domain.model.AuthPerfilModel;
import br.com.projeto.piloto.accesscontrol.domain.model.AuthUsuarioModel;
import jakarta.servlet.ServletException;

@WebMvcTest(UsuarioController.class)
@ContextConfiguration(classes = UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
	@MockBean
    private AuthUsuarioUseCasePort usuarioUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    private AuthUsuarioRequestDTO validDto;
    private AuthUsuarioModel validModel;

    @BeforeEach
    void setup() {
        validDto = new AuthUsuarioRequestDTO(
                "admin", "senha123", "Nome Admin", "S", "admin@teste.com", Set.of(1L));

        AuthPerfilModel perfil = AuthPerfilModel.builder()
                .id(1L)
                .nmPerfil("ADMIN")
                .build();

        validModel = AuthUsuarioModel.builder()
                .id(1L)
                .login("admin")
                .nome("Nome Admin")
                .ativo("S")
                .email("admin@teste.com")
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .perfis(Set.of(perfil))
                .build();
    }

    @Test
    @DisplayName("POST - Criar usuário com sucesso (201)")
    void createSuccess() throws Exception {
        when(usuarioUseCase.criar(any())).thenReturn(validModel);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("admin"))
                .andExpect(jsonPath("$.nome").value("Nome Admin"))
                .andExpect(jsonPath("$.ativo").value("S"));
    }

    @Test
    @DisplayName("POST - Criar usuário com DataIntegrityViolation (catch relançado)")
    void createDataIntegrityViolation() {
        when(usuarioUseCase.criar(any())).thenThrow(new DataIntegrityViolationException("Conflito"));

        ServletException ex = assertThrows(ServletException.class, () ->
                mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto))));

        assertInstanceOf(DataIntegrityViolationException.class, ex.getCause());
    }

    @Test
    @DisplayName("POST - normalizarAtivo com valor inválido normaliza para 'N'")
    void createAtivoInvalido() throws Exception {
        AuthUsuarioRequestDTO dtoInvalido = new AuthUsuarioRequestDTO(
                "user2", "pass", "Nome", "X", "x@teste.com", Set.of(1L));

        AuthUsuarioModel modelInativo = AuthUsuarioModel.builder()
                .id(2L).login("user2").nome("Nome").ativo("N")
                .email("x@teste.com").perfis(Set.of(
                        AuthPerfilModel.builder().id(1L).nmPerfil("ADMIN").build()))
                .build();

        when(usuarioUseCase.criar(any())).thenReturn(modelInativo);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ativo").value("N"));
    }

    @Test
    @DisplayName("POST - normalizarAtivo com null normaliza para 'N'")
    void createAtivoNull() throws Exception {
        AuthUsuarioRequestDTO dtoNull = new AuthUsuarioRequestDTO(
                "user3", "pass", "Nome", null, "n@teste.com", Set.of(1L));

        when(usuarioUseCase.criar(any())).thenReturn(validModel);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoNull)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST - normalizarAtivo com 'N' retorna 'N'")
    void createAtivoN() throws Exception {
        AuthUsuarioRequestDTO dtoN = new AuthUsuarioRequestDTO(
                "user4", "pass", "Nome", "N", "n@teste.com", Set.of(1L));

        when(usuarioUseCase.criar(any())).thenReturn(validModel);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoN)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST - perfisIds nulo lança IllegalArgumentException")
    void createPerfisNull() {
        AuthUsuarioRequestDTO dtoSemPerfis = new AuthUsuarioRequestDTO(
                "user5", "pass", "Nome", "S", "s@teste.com", null);

        ServletException ex = assertThrows(ServletException.class, () ->
                mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoSemPerfis))));

        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
    }

    @Test
    @DisplayName("POST - perfisIds vazio lança IllegalArgumentException")
    void createPerfisVazio() {
        AuthUsuarioRequestDTO dtoVazio = new AuthUsuarioRequestDTO(
                "user6", "pass", "Nome", "S", "v@teste.com", Collections.emptySet());

        ServletException ex = assertThrows(ServletException.class, () ->
                mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoVazio))));

        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
    }

    @Test
    @DisplayName("POST - perfisIds com id null lança IllegalArgumentException")
    void createPerfisComIdNulo() {
        Set<Long> idsComNulo = new HashSet<>();
        idsComNulo.add(null);
        AuthUsuarioRequestDTO dtoComNulo = new AuthUsuarioRequestDTO(
                "user7", "pass", "Nome", "S", "c@teste.com", idsComNulo);

        ServletException ex = assertThrows(ServletException.class, () ->
                mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoComNulo))));

        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
    }

    @Test
    @DisplayName("PUT - Atualizar usuário com sucesso (200)")
    void updateSuccess() throws Exception {
        when(usuarioUseCase.atualizar(eq(1L), any())).thenReturn(validModel);

        mockMvc.perform(put("/api/usuarios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.login").value("admin"));
    }

    @Test
    @DisplayName("PUT - Atualizar com UserNotFoundException (catch relançado)")
    void updateUserNotFound() {
        when(usuarioUseCase.atualizar(eq(99L), any()))
                .thenThrow(new UserNotFoundException("Usuário com ID 99 não encontrado."));

        ServletException ex = assertThrows(ServletException.class, () ->
                mockMvc.perform(put("/api/usuarios/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto))));

        assertInstanceOf(UserNotFoundException.class, ex.getCause());
    }

    @Test
    @DisplayName("PUT - Atualizar com DataIntegrityViolationException (catch relançado)")
    void updateDataIntegrityViolation() {
        when(usuarioUseCase.atualizar(eq(1L), any()))
                .thenThrow(new DataIntegrityViolationException("Conflito"));

        ServletException ex = assertThrows(ServletException.class, () ->
                mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto))));

        assertInstanceOf(DataIntegrityViolationException.class, ex.getCause());
    }

    @Test
    @DisplayName("DELETE - Remover usuário com sucesso (204)")
    void deleteSuccess() throws Exception {
        doNothing().when(usuarioUseCase).deletar(1L);

        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE - Remover usuário lança UserNotFoundException")
    void deleteUserNotFound() {
        doThrow(new UserNotFoundException("Usuário com ID 99 não encontrado."))
                .when(usuarioUseCase).deletar(99L);

        assertThrows(ServletException.class, () ->
                mockMvc.perform(delete("/api/usuarios/99")));
    }

    @Test
    @DisplayName("GET /{id} - Buscar usuário por ID com sucesso (200)")
    void findByIdSuccess() throws Exception {
        when(usuarioUseCase.buscarPorId(1L)).thenReturn(validModel);

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.login").value("admin"));
    }

    @Test
    @DisplayName("GET /{id} - Buscar usuário por ID lança UserNotFoundException")
    void findByIdNotFound() {
        when(usuarioUseCase.buscarPorId(99L))
                .thenThrow(new UserNotFoundException("Usuário com ID 99 não encontrado."));

        assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/api/usuarios/99")));
    }

    @Test
    @DisplayName("GET - Listar todos os usuários com registros (200)")
    void listAllSuccess() throws Exception {
        when(usuarioUseCase.listarTodos()).thenReturn(List.of(validModel));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].login").value("admin"))
                .andExpect(jsonPath("$[0].perfisIds[0].nmPerfil").value("ADMIN"));
    }

    @Test
    @DisplayName("GET - Listar todos os usuários com lista vazia (200)")
    void listAllEmpty() throws Exception {
        when(usuarioUseCase.listarTodos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
