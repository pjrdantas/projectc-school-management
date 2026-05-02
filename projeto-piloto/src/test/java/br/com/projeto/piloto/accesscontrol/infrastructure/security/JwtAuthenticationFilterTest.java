package br.com.projeto.piloto.accesscontrol.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import br.com.projeto.piloto.accesscontrol.application.service.AuthSessaoService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtAuthenticationFilterTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private UserDetailsService userDetailsService;
    @Mock private AuthSessaoService authSessaoService; 
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @SuppressWarnings("null")
	@Test
    @DisplayName("Deve permitir acesso sem token (Header ausente)")
    void devePermitirSemToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @SuppressWarnings("null")
	@Test
    @DisplayName("Deve autenticar usuário quando token é válido e sessão ativa")
    void deveAutenticarComSucesso() throws ServletException, IOException {
        String token = "token.valido";
        String username = "admin";
        UserDetails userDetails = new User(username, "", Collections.emptyList());

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validate(token)).thenReturn(true);
        when(jwtUtil.getUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(authSessaoService.validarSessao(token)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(username, SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @SuppressWarnings("null")
	@Test
    @DisplayName("Deve ignorar token mal formatado (Sem Bearer)")
    void deveIgnorarTokenMalFormatado() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("TokenInvalido 123");
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @SuppressWarnings("null")
	@Test
    @DisplayName("Deve barrar usuário quando sessão no banco está inativa")
    void deveBarrarSessaoInativaNoBanco() throws ServletException, IOException {
        String token = "token.valido";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validate(token)).thenReturn(true);
        when(authSessaoService.validarSessao(token)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }


    @SuppressWarnings("null")
	@Test
    @DisplayName("Deve ignorar quando jwtUtil.validate retorna false")
    void deveIgnorarQuandoValidateFalse() throws ServletException, IOException {
        String token = "bad.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validate(token)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @SuppressWarnings("null")
	@Test
    @DisplayName("Deve não autenticar quando username é nulo")
    void naoAutenticaQuandoUsernameNull() throws ServletException, IOException {
        String token = "token.semuser";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validate(token)).thenReturn(true);
        when(authSessaoService.validarSessao(token)).thenReturn(true);
        when(jwtUtil.getUsername(token)).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @SuppressWarnings("null")
	@Test
    @DisplayName("Deve limpar contexto quando jwtUtil lança exceção")
    void limpaContextoQuandoJwtUtilLanca() throws ServletException, IOException {
        String token = "throw.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validate(token)).thenThrow(new RuntimeException("boom"));

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @SuppressWarnings("null")
	@Test
    @DisplayName("Deve limpar contexto quando userDetailsService lança exceção")
    void limpaContextoQuandoUserDetailsServiceLanca() throws ServletException, IOException {
        String token = "token.exc.user";
        String username = "userX";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validate(token)).thenReturn(true);
        when(authSessaoService.validarSessao(token)).thenReturn(true);
        when(jwtUtil.getUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenThrow(new RuntimeException("user fail"));

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @SuppressWarnings("null")
	@Test
    @DisplayName("Não substitui authentication se já existir no contexto")
    void naoSubstituiAuthenticationSeExistir() throws ServletException, IOException {
        String token = "token.existing";
        String username = "adminExisting";
        UsernamePasswordAuthenticationToken existing = new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(existing);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validate(token)).thenReturn(true);
        when(authSessaoService.validarSessao(token)).thenReturn(true);
        when(jwtUtil.getUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(new User(username, "", Collections.emptyList()));

        filter.doFilter(request, response, filterChain);

        assertEquals(existing, SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
