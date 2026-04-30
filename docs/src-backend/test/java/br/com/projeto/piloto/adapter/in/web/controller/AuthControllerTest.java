
package br.com.projeto.piloto.adapter.in.web.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.projeto.piloto.adapter.in.web.dto.LoginRequestDTO;
import br.com.projeto.piloto.adapter.in.web.dto.RefreshTokenRequestDTO;
import br.com.projeto.piloto.application.service.AuthSessaoService;
import br.com.projeto.piloto.application.usecase.AuthInteractor;
import br.com.projeto.piloto.domain.model.AuthPerfilModel;
import br.com.projeto.piloto.domain.model.AuthPermissaoModel;
import br.com.projeto.piloto.domain.model.AuthSessaoModel;
import br.com.projeto.piloto.domain.model.AuthUsuarioModel;
import br.com.projeto.piloto.infrastructure.security.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;

@WebMvcTest(AuthController.class)
@ContextConfiguration(classes = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("removal")
	@MockBean
    private AuthInteractor authInteractor;

    @SuppressWarnings("removal")
	@MockBean
    private JwtUtil jwtUtil;

    @SuppressWarnings("removal")
	@MockBean
    private AuthSessaoService authSessaoService;

    private AuthUsuarioModel usuario;

    @BeforeEach
    void setUp() {
        AuthPerfilModel perfil = AuthPerfilModel.builder()
                .id(1L)
                .nmPerfil("ROLE_USER")
                .build();

        usuario = AuthUsuarioModel.builder()
                .id(42L)
                .login("jdoe")
                .nome("John Doe")
                .ativo("S")
                .email("jdoe@teste.com")
                .perfis(Set.of(perfil))
                .build();
    }


    @Test
    @DisplayName("LOGIN - Sucesso: roles e permissões incluídas no token (maiúsculas)")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void login_comRolesEPermissoes_sucesso() throws Exception {
        AuthPermissaoModel perm = new AuthPermissaoModel();
        perm.setId(100L);
        perm.setNmPermissao("read_items");

        AuthPerfilModel perfil = AuthPerfilModel.builder()
                .id(2L).nmPerfil("ROLE_ADMIN").build();
        perfil.setPermissoes(Set.of(perm));

        usuario.setPerfis(Set.of(perfil));

        when(authInteractor.authenticate("jdoe", "pwd")).thenReturn(usuario);
        when(jwtUtil.generateToken(anyString(), anySet())).thenReturn("tok1");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("ref1");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequestDTO("jdoe", "pwd"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("tok1"))
                .andExpect(jsonPath("$.refreshToken").value("ref1"))
                .andExpect(jsonPath("$.usuario").value("jdoe"))
                .andExpect(jsonPath("$.nome").value("John Doe"));

        ArgumentCaptor<Set> cap = ArgumentCaptor.forClass(Set.class);
        verify(jwtUtil).generateToken(eq("jdoe"), cap.capture());
        Set<String> authorities = cap.getValue();
        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertTrue(authorities.contains("READ_ITEMS"));

        verify(authSessaoService).criarSessao(42L, "tok1", "ref1");
    }

    @Test
    @DisplayName("LOGIN - Perfil com nome null/blank e permissões null/nmPermissao null são ignorados")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void login_ignoraNomesNulosEBrancos_ePermissoesNulas() throws Exception {
        AuthPerfilModel p1 = new AuthPerfilModel();
        p1.setNmPerfil(null);
        AuthPerfilModel p2 = new AuthPerfilModel();
        p2.setNmPerfil("   ");
        AuthPermissaoModel permNula = null;
        AuthPermissaoModel permSemNome = new AuthPermissaoModel();
        permSemNome.setNmPermissao(null);
        AuthPerfilModel p3 = AuthPerfilModel.builder().id(3L).nmPerfil("ROLE_OK").build();
        Set<AuthPermissaoModel> perms = new HashSet<>();
        perms.add(permNula);
        perms.add(permSemNome);
        p3.setPermissoes(perms);
        AuthPerfilModel p4 = AuthPerfilModel.builder().id(4L).nmPerfil("ROLE_B").build();
        p4.setPermissoes(null);

        usuario.setPerfis(Set.of(p1, p2, p3, p4));

        when(authInteractor.authenticate("jdoe", "x")).thenReturn(usuario);
        when(jwtUtil.generateToken(anyString(), anySet())).thenReturn("tkn");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("rfn");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequestDTO("jdoe", "x"))))
                .andExpect(status().isOk());

        ArgumentCaptor<Set> cap = ArgumentCaptor.forClass(Set.class);
        verify(jwtUtil).generateToken(eq("jdoe"), cap.capture());
        Set<String> authorities = cap.getValue();
        assertTrue(authorities.contains("ROLE_OK"));
        assertTrue(authorities.contains("ROLE_B"));
    }


    @Test
    @DisplayName("VALIDATE-SESSION - Token ausente (chave não presente no map) → 400")
    void validateSession_tokenAusente_retorna400() throws Exception {
        mockMvc.perform(post("/api/auth/validate-session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Token não fornecido"));
    }

    @Test
    @DisplayName("VALIDATE-SESSION - Token em branco → 400")
    void validateSession_tokenEmBranco_retorna400() throws Exception {
        mockMvc.perform(post("/api/auth/validate-session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("token", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Token não fornecido"));
    }

    @Test
    @DisplayName("VALIDATE-SESSION - Sessão inválida → 401")
    void validateSession_sessaoInvalida_retorna401() throws Exception {
        when(authSessaoService.validarSessao("bad")).thenReturn(false);

        mockMvc.perform(post("/api/auth/validate-session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("token", "bad"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Sessão inválida ou expirada. Faça login novamente."));
    }

    @Test
    @DisplayName("VALIDATE-SESSION - Sessão válida → 200 com valid=true")
    void validateSession_sessaoValida_retorna200() throws Exception {
        when(authSessaoService.validarSessao("ok")).thenReturn(true);

        mockMvc.perform(post("/api/auth/validate-session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("token", "ok"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    @DisplayName("VALIDATE-SESSION - Exception inesperada → 401 com mensagem genérica")
    void validateSession_exception_retorna401() throws Exception {
        when(authSessaoService.validarSessao("boom")).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/auth/validate-session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("token", "boom"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Sessão inválida."));
    }

    @Test
    @DisplayName("REFRESH - Token em branco → 400 com mensagem adequada")
    void refresh_tokenEmBranco_retorna400() throws Exception {
        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshTokenRequestDTO(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O token de refresh não pode ser nulo ou vazio"));
    }

    @Test
    @DisplayName("REFRESH - Token inválido (validate=false) → 401")
    void refresh_tokenInvalido_retorna401() throws Exception {
        when(jwtUtil.validate("inv")).thenReturn(false);

        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshTokenRequestDTO("inv"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token inválido ou expirado"));
    }

    @Test
    @DisplayName("REFRESH - Sessão não encontrada → 401")
    void refresh_sessaoNaoEncontrada_retorna401() throws Exception {
        when(jwtUtil.validate("s1")).thenReturn(true);
        when(authSessaoService.encontrarPorRefreshToken("s1")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshTokenRequestDTO("s1"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Sessão inválida ou expirada. Faça login novamente."));
    }

    @Test
    @DisplayName("REFRESH - Sucesso: novo accessToken gerado com roles e permissões")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void refresh_sucesso_novoTokenGerado() throws Exception {
        AuthPermissaoModel perm = new AuthPermissaoModel();
        perm.setNmPermissao("perm_ok");

        AuthPerfilModel pf = AuthPerfilModel.builder().id(1L).nmPerfil("role_z").build();
        pf.setPermissoes(Set.of(perm));
        usuario.setPerfis(Set.of(pf));

        when(jwtUtil.validate("ok1")).thenReturn(true);
        when(authSessaoService.encontrarPorRefreshToken("ok1")).thenReturn(Optional.of(new AuthSessaoModel()));
        when(jwtUtil.extractUsernameFromRefreshToken("ok1")).thenReturn("jdoe");
        when(authInteractor.findByLogin("jdoe")).thenReturn(usuario);
        when(jwtUtil.generateToken(anyString(), anySet())).thenReturn("newtk");

        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshTokenRequestDTO("ok1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newtk"));

        ArgumentCaptor<Set> cap = ArgumentCaptor.forClass(Set.class);
        verify(jwtUtil).generateToken(eq("jdoe"), cap.capture());
        Set<String> authorities = cap.getValue();
        assertTrue(authorities.contains("ROLE_Z"));
        assertTrue(authorities.contains("PERM_OK"));
    }

    @Test
    @DisplayName("REFRESH - Sucesso: perfil com permissoes null não causa NPE")
    void refresh_sucesso_permissoesNulas_naoLancaErro() throws Exception {
        AuthPerfilModel pfSemPerms = AuthPerfilModel.builder().id(2L).nmPerfil("ROLE_X").build();
        pfSemPerms.setPermissoes(null);
        usuario.setPerfis(Set.of(pfSemPerms));

        when(jwtUtil.validate("ok2")).thenReturn(true);
        when(authSessaoService.encontrarPorRefreshToken("ok2")).thenReturn(Optional.of(new AuthSessaoModel()));
        when(jwtUtil.extractUsernameFromRefreshToken("ok2")).thenReturn("jdoe");
        when(authInteractor.findByLogin("jdoe")).thenReturn(usuario);
        when(jwtUtil.generateToken(anyString(), anySet())).thenReturn("tk2");

        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshTokenRequestDTO("ok2"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("tk2"));
    }

    @Test
    @DisplayName("REFRESH - Sucesso: permissão null e nmPermissao null dentro do set são ignorados")
    void refresh_sucesso_permissoesComNulosIgnoradas() throws Exception {
        AuthPermissaoModel permNull = null;
        AuthPermissaoModel permSemNome = new AuthPermissaoModel();
        permSemNome.setNmPermissao(null);

        Set<AuthPermissaoModel> perms = new HashSet<>();
        perms.add(permNull);
        perms.add(permSemNome);

        AuthPerfilModel pf = AuthPerfilModel.builder().id(3L).nmPerfil("ROLE_Y").build();
        pf.setPermissoes(perms);
        usuario.setPerfis(Set.of(pf));

        when(jwtUtil.validate("ok3")).thenReturn(true);
        when(authSessaoService.encontrarPorRefreshToken("ok3")).thenReturn(Optional.of(new AuthSessaoModel()));
        when(jwtUtil.extractUsernameFromRefreshToken("ok3")).thenReturn("jdoe");
        when(authInteractor.findByLogin("jdoe")).thenReturn(usuario);
        when(jwtUtil.generateToken(anyString(), anySet())).thenReturn("tk3");

        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshTokenRequestDTO("ok3"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("tk3"));
    }

    @Test
    @DisplayName("REFRESH - Perfil com nome null/blank filtrado pelo stream")
    void refresh_perfilComNomeNulo_filtradoNoStream() throws Exception {
        AuthPerfilModel pfNull = new AuthPerfilModel();
        pfNull.setNmPerfil(null);

        AuthPerfilModel pfBlank = new AuthPerfilModel();
        pfBlank.setNmPerfil("  ");

        usuario.setPerfis(Set.of(pfNull, pfBlank));

        when(jwtUtil.validate("ok4")).thenReturn(true);
        when(authSessaoService.encontrarPorRefreshToken("ok4")).thenReturn(Optional.of(new AuthSessaoModel()));
        when(jwtUtil.extractUsernameFromRefreshToken("ok4")).thenReturn("jdoe");
        when(authInteractor.findByLogin("jdoe")).thenReturn(usuario);
        when(jwtUtil.generateToken(anyString(), anySet())).thenReturn("tk4");

        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshTokenRequestDTO("ok4"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("tk4"));
    }

    @Test
    @DisplayName("REFRESH - ExpiredJwtException → 401 com mensagem de expiração")
    void refresh_expiredJwt_retorna401() throws Exception {
        doThrow(new ExpiredJwtException(null, null, "expired"))
                .when(jwtUtil).validate("exp");

        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshTokenRequestDTO("exp"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("O token de refresh expirou"));
    }

    @Test
    @DisplayName("REFRESH - MalformedJwtException → 400 com mensagem de token malformado")
    void refresh_malformedJwt_retorna400() throws Exception {
        doThrow(new MalformedJwtException("malformed"))
                .when(jwtUtil).validate("mal");

        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshTokenRequestDTO("mal"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O token de refresh fornecido é inválido ou malformado"));
    }

    @Test
    @DisplayName("REFRESH - JwtException genérica → 500 com mensagem de erro interno")
    void refresh_jwtException_retorna500() throws Exception {
        doThrow(new JwtException("generic error"))
                .when(jwtUtil).validate("je");

        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshTokenRequestDTO("je"))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Erro ao processar o token de refresh: generic error"));
    }

    @Test
    @DisplayName("REFRESH - JwtException ao extrair username após sessão válida → 500")
    void refresh_jwtExceptionNoExtract_retorna500() throws Exception {
        when(jwtUtil.validate("exu")).thenReturn(true);
        when(authSessaoService.encontrarPorRefreshToken("exu")).thenReturn(Optional.of(new AuthSessaoModel()));
        doThrow(new JwtException("bad extract"))
                .when(jwtUtil).extractUsernameFromRefreshToken("exu");

        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshTokenRequestDTO("exu"))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }
}
