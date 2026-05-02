package br.com.projeto.piloto.accesscontrol.application.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.entity.AuthSessao;
import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.repository.AuthSessaoRepository;
import br.com.projeto.piloto.accesscontrol.domain.model.AuthSessaoModel;
import br.com.projeto.piloto.accesscontrol.infrastructure.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
class AuthSessaoServiceTest {

    @Mock
    private AuthSessaoRepository authSessaoRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthSessaoService authSessaoService;

    private final String TOKEN = "token-123";
    private final String REFRESH = "refresh-123";

    @BeforeEach
    void setup() {
    }

    @SuppressWarnings("null")
	@Test
    @DisplayName("criarSessao deve criar sessão e mapear para model")
    void criarSessao_success() {
        LocalDateTime expiration = LocalDateTime.now().plusHours(1);
        when(jwtUtil.extractExpiration(TOKEN)).thenReturn(expiration);

        AuthSessao saved = AuthSessao.builder()
                .id(10L)
                .authUsuarioId(5L)
                .token(TOKEN)
                .refreshToken(REFRESH)
                .dataCriacao(LocalDateTime.now())
                .dataExpiracao(expiration)
                .ativo("S")
                .build();

        when(authSessaoRepository.save(any(AuthSessao.class))).thenReturn(saved);

        AuthSessaoModel model = authSessaoService.criarSessao(5L, TOKEN, REFRESH);

        assertNotNull(model);
        assertEquals(10L, model.getId());
        assertEquals(5L, model.getAuthUsuarioId());
        assertEquals(TOKEN, model.getToken());
        assertEquals(REFRESH, model.getRefreshToken());
        assertEquals(expiration, model.getDataExpiracao());

        verify(authSessaoRepository).invalidarTodasSessoes(5L);
        verify(authSessaoRepository).deletarSessoesInativas(5L);
        verify(authSessaoRepository).save(any(AuthSessao.class));
    }

    @Test
    @DisplayName("validarSessao retorna true quando sessão presente e ativa")
    void validarSessao_true() {
        AuthSessao sessao = AuthSessao.builder()
                .id(1L)
                .authUsuarioId(2L)
                .token(TOKEN)
                .ativo("S")
                .dataExpiracao(LocalDateTime.now().plusMinutes(30))
                .build();

        when(authSessaoRepository.findByTokenAndAtivoAndDataExpiracaoAfter(eq(TOKEN), eq("S"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(sessao));

        boolean valid = authSessaoService.validarSessao(TOKEN);
        assertTrue(valid);
    }

    @Test
    @DisplayName("validarSessao retorna false quando sessão não encontrada")
    void validarSessao_false() {
        when(authSessaoRepository.findByTokenAndAtivoAndDataExpiracaoAfter(eq(TOKEN), eq("S"), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        boolean valid = authSessaoService.validarSessao(TOKEN);
        assertFalse(valid);
    }

    @Test
    @DisplayName("encontrarPorRefreshToken retorna model quando sessão ativa e não expirada")
    void encontrarPorRefreshToken_present() {
        AuthSessao sessao = AuthSessao.builder()
                .id(2L)
                .authUsuarioId(7L)
                .token(TOKEN)
                .refreshToken(REFRESH)
                .dataCriacao(LocalDateTime.now())
                .dataExpiracao(LocalDateTime.now().plusMinutes(5))
                .ativo("S")
                .build();

        when(authSessaoRepository.findByRefreshToken(REFRESH)).thenReturn(Optional.of(sessao));

        Optional<AuthSessaoModel> result = authSessaoService.encontrarPorRefreshToken(REFRESH);
        assertTrue(result.isPresent());
        AuthSessaoModel model = result.get();
        assertEquals(2L, model.getId());
        assertEquals(7L, model.getAuthUsuarioId());
        assertEquals(REFRESH, model.getRefreshToken());
    }

    @Test
    @DisplayName("encontrarPorRefreshToken retorna empty quando sessão não ativa")
    void encontrarPorRefreshToken_notActive() {
        AuthSessao sessao = AuthSessao.builder()
                .id(3L)
                .authUsuarioId(8L)
                .refreshToken(REFRESH)
                .dataCriacao(LocalDateTime.now())
                .dataExpiracao(LocalDateTime.now().plusMinutes(5))
                .ativo("N")
                .build();

        when(authSessaoRepository.findByRefreshToken(REFRESH)).thenReturn(Optional.of(sessao));

        Optional<AuthSessaoModel> result = authSessaoService.encontrarPorRefreshToken(REFRESH);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("encontrarPorRefreshToken retorna empty quando sessão expirada")
    void encontrarPorRefreshToken_expired() {
        AuthSessao sessao = AuthSessao.builder()
                .id(4L)
                .authUsuarioId(9L)
                .refreshToken(REFRESH)
                .dataCriacao(LocalDateTime.now().minusDays(2))
                .dataExpiracao(LocalDateTime.now().minusHours(1))
                .ativo("S")
                .build();

        when(authSessaoRepository.findByRefreshToken(REFRESH)).thenReturn(Optional.of(sessao));

        Optional<AuthSessaoModel> result = authSessaoService.encontrarPorRefreshToken(REFRESH);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("invalidarSessao delega para repository")
    void invalidarSessao_callsRepository() {
        authSessaoService.invalidarSessao(11L);
        verify(authSessaoRepository).invalidarSessao(11L);
    }

    @Test
    @DisplayName("invalidarTodasSessoes delega para repository")
    void invalidarTodasSessoes_callsRepository() {
        authSessaoService.invalidarTodasSessoes(12L);
        verify(authSessaoRepository).invalidarTodasSessoes(12L);
    }
}
