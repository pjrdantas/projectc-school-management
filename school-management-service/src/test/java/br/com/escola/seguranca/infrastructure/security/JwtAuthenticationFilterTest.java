package br.com.escola.seguranca.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.escola.seguranca.application.dto.internal.PrincipalAutenticadoResumo;
import br.com.escola.seguranca.application.port.internal.IdentidadeTenantPort;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private IdentidadeTenantPort identidadeTenantPort;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAutenticarContextoQuandoBearerForValido() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(identidadeTenantPort);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(identidadeTenantPort.resolverPrincipal("token-valido"))
                .thenReturn(new PrincipalAutenticadoResumo(
                        UUID.randomUUID(),
                        "professor52",
                        List.of("PROFESSOR_LEITURA", "PLANEJAMENTO_LEITURA")));

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isInstanceOf(UserDetails.class);
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        assertThat(principal.getUsername()).isEqualTo("professor52");
        assertThat(principal.getAuthorities())
                .extracting(grantedAuthority -> grantedAuthority.getAuthority())
                .containsExactlyInAnyOrder("PROFESSOR_LEITURA", "PLANEJAMENTO_LEITURA");
        verify(identidadeTenantPort).resolverPrincipal("token-valido");
    }

    @Test
    void deveLimparContextoQuandoTokenForInvalido() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(identidadeTenantPort);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-invalido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(identidadeTenantPort.resolverPrincipal("token-invalido"))
                .thenThrow(new RuntimeException("token inválido"));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(identidadeTenantPort).resolverPrincipal("token-invalido");
    }
}
