package br.com.escola.accesscontrol.infrastructure.security;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.escola.accesscontrol.adapter.out.persistence.entity.UsuarioEntity;
import br.com.escola.accesscontrol.application.service.AuthService;
import br.com.escola.accesscontrol.domain.exception.TokenInvalidoOuExpiradoException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AccessTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AccessTokenAuthenticationFilter.class);

    private final AuthService authService;

    public AccessTokenAuthenticationFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String normalizedHeader = authHeader.trim();
            if (normalizedHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
                String token = normalizedHeader.substring(7).trim();
                if (!token.isEmpty()) {
                    try {
                        UsuarioEntity usuario = authService.validarAccessToken(token);
                        List<SimpleGrantedAuthority> authorities = authService.buscarPermissoes(usuario.getId()).stream()
                                .map(SimpleGrantedAuthority::new)
                                .toList();

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(usuario.getUsername(), null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } catch (TokenInvalidoOuExpiradoException ex) {
                        SecurityContextHolder.clearContext();
                    } catch (RuntimeException ex) {
                        SecurityContextHolder.clearContext();
                        log.warn("Falha ao processar token de acesso", ex);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
