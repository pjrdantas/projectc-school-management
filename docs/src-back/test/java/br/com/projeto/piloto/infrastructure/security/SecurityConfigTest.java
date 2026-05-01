package br.com.projeto.piloto.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.HttpServletRequest;

import br.com.projeto.piloto.application.service.AuthSessaoService;  

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SecurityConfigTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private UserDetailsService userDetailsService;
    @Mock private AuthenticationConfiguration authConfig;
    @Mock private AuthSessaoService authSessaoService;  

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        
        securityConfig = new SecurityConfig(jwtUtil, userDetailsService, authSessaoService);
    }

    @Test
    @DisplayName("Cobre Beans de Auth e Password")
    void deveCobrirBeansSimples() throws Exception {
         
        assertNotNull(securityConfig.passwordEncoder());

        var authManager = mock(org.springframework.security.authentication.AuthenticationManager.class);

        when(authConfig.getAuthenticationManager()).thenReturn(authManager);

        assertNotNull(securityConfig.authenticationManager(authConfig));
    }


    @SuppressWarnings("null")
	@Test
    @DisplayName("Cobre configuração de CORS - Sem NullPointerException")
    void deveCobrirConfiguracaoCorsCompleta() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletMapping mapping = mock(HttpServletMapping.class);

        lenient().when(request.getContextPath()).thenReturn("");
        lenient().when(request.getServletPath()).thenReturn("");
        lenient().when(request.getPathInfo()).thenReturn("");
        lenient().when(request.getRequestURI()).thenReturn("/");
        lenient().when(request.getHttpServletMapping()).thenReturn(mapping);

        CorsConfiguration config = source.getCorsConfiguration(request);
        
        assertNotNull(config);
        assertEquals(List.of("*"), config.getAllowedOrigins());
        assertEquals(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"), config.getAllowedMethods());
        assertFalse(config.getAllowCredentials());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    @DisplayName("Cobre FilterChain, Lambdas e o método build()")
    void deveCobrindoFilterChainELambdas() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class);

        DefaultSecurityFilterChain mockChain = mock(DefaultSecurityFilterChain.class);
        when(http.build()).thenReturn(mockChain);

        when(http.csrf(any(Customizer.class))).thenAnswer(inv -> {
            ((Customizer) inv.getArgument(0)).customize(mock(CsrfConfigurer.class));
            return http;
        });

        when(http.cors(any(Customizer.class))).thenAnswer(inv -> {
            ((Customizer) inv.getArgument(0)).customize(mock(CorsConfigurer.class));
            return http;
        });

        when(http.sessionManagement(any(Customizer.class))).thenAnswer(inv -> {
            ((Customizer) inv.getArgument(0)).customize(mock(SessionManagementConfigurer.class));
            return http;
        });

        when(http.authorizeHttpRequests(any(Customizer.class))).thenAnswer(inv -> {
            Customizer customizer = inv.getArgument(0);
            var registry = mock(AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class);
            var authorizedUrl = mock(AuthorizeHttpRequestsConfigurer.AuthorizedUrl.class);
            
            when(registry.requestMatchers(any(String[].class))).thenReturn(authorizedUrl);
            when(registry.requestMatchers(anyString())).thenReturn(authorizedUrl);
            when(authorizedUrl.permitAll()).thenReturn(registry);
            when(registry.anyRequest()).thenReturn(authorizedUrl);
            when(authorizedUrl.authenticated()).thenReturn(registry);
            
            customizer.customize(registry);
            return http;
        });

        when(http.addFilterBefore(any(), any())).thenReturn(http);

        SecurityFilterChain result = securityConfig.filterChain(http);

        assertNotNull(result);
        verify(http).build();
    }
}