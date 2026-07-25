package br.com.escola.bff.interfaces.web;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.interfaces.response.ApiErrorResponse;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class BearerAuthenticationWebFilter implements WebFilter {

    private static final Set<String> UNTRUSTED_CONTEXT_HEADERS = Set.of(
            "X-Usuario-Id", "X-User-Id", "X-Escola-Id", "X-Tenant-Id");

    private final ObjectMapper objectMapper;

    public BearerAuthenticationWebFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (!isProtectedCatalogReadRoute(exchange)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")
                || authorization.substring(7).isBlank()) {
            return unauthorized(exchange);
        }

        ServerHttpRequest sanitized = exchange.getRequest().mutate()
                .headers(headers -> UNTRUSTED_CONTEXT_HEADERS.forEach(headers::remove))
                .build();
        return chain.filter(exchange.mutate().request(sanitized).build());
    }

    private boolean isProtectedCatalogReadRoute(ServerWebExchange exchange) {
        HttpMethod method = exchange.getRequest().getMethod();
        if (method == null) {
            return false;
        }
        String path = exchange.getRequest().getPath().value();
        if (path.matches("^/api/(usuarios|perfis|permissoes)(/[^/]+)?$")) {
            return true;
        }
        if (HttpMethod.POST.equals(method) && "/api/periodos-letivos".equals(path)) {
            return true;
        }
        if (HttpMethod.POST.equals(method) && "/api/escolas-origem".equals(path)) {
            return true;
        }
        if (HttpMethod.POST.equals(method) && "/api/auth/escola-ativa".equals(path)) {
            return true;
        }
        if (HttpMethod.POST.equals(method) && "/api/transferencias".equals(path)) {
            return true;
        }
        if (HttpMethod.POST.equals(method) && "/api/disciplinas".equals(path)) {
            return true;
        }
        if (HttpMethod.POST.equals(method) && "/api/series".equals(path)) {
            return true;
        }
        if (HttpMethod.POST.equals(method) && "/api/turmas".equals(path)) {
            return true;
        }
        if (HttpMethod.POST.equals(method) && path.matches("^/api/turmas/[^/]+/disciplinas$")) {
            return true;
        }
        if (!HttpMethod.GET.equals(method)) {
            return false;
        }
        return "/api/disciplinas".equals(path)
                || "/api/auth/tenant/ativa".equals(path)
                || "/api/auth/escolas".equals(path)
                || path.matches("^/api/disciplinas/[^/]+$")
                || "/api/periodos-letivos".equals(path)
                || path.matches("^/api/periodos-letivos/[^/]+$")
                || "/api/series".equals(path)
                || path.matches("^/api/series/[^/]+$")
                || "/api/turnos".equals(path)
                || path.matches("^/api/turnos/[^/]+$")
                || "/api/turmas".equals(path)
                || path.matches("^/api/turmas/[^/]+$")
                || path.matches("^/api/turmas/[^/]+/disciplinas$")
                || "/api/academico/catalogos/niveis-ensino".equals(path)
                || "/api/academico/catalogos/turnos".equals(path)
                || "/api/professores".equals(path)
                || "/api/professores/funcionarios-elegiveis".equals(path)
                || path.matches("^/api/professores/[^/]+$")
                || path.matches("^/api/professores/[^/]+/turmas-disciplinas$")
                || path.matches("^/api/turmas/[^/]+/professores$")
                || "/api/funcionarios".equals(path)
                || path.matches("^/api/funcionarios/[^/]+$")
                || "/api/consulta-cadastral".equals(path)
                || "/api/pessoas/catalogos/tipos-pessoa".equals(path)
                || "/api/pessoas/catalogos/tipos-endereco".equals(path)
                || "/api/pessoas/catalogos/status-aluno".equals(path)
                || "/api/pessoas/catalogos/parentescos".equals(path)
                || path.matches("^/api/pessoas/[^/]+$")
                || path.matches("^/api/pessoas/[^/]+/endereco-principal$")
                || path.matches("^/api/pessoas/[^/]+/enderecos$")
                || path.matches("^/api/pessoas/[^/]+/contato$")
                || path.matches("^/api/pessoas/[^/]+/documentos$")
                || "/api/documentos".equals(path)
                || path.matches("^/api/documentos/[^/]+$")
                || path.matches("^/api/documentos-alunos/alunos/[^/]+$")
                || "/api/matriculas".equals(path)
                || path.matches("^/api/matriculas/[^/]+$");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String correlationId = exchange.getRequest().getHeaders()
                .getFirst(TrustedHeaders.CORRELATION_ID);
        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                "Bearer token obrigatorio",
                exchange.getRequest().getPath().value(),
                correlationId);
        try {
            byte[] body = objectMapper.writeValueAsBytes(error);
            return exchange.getResponse().writeWith(Mono.just(
                    exchange.getResponse().bufferFactory().wrap(body)));
        } catch (JacksonException exception) {
            byte[] body = "{\"code\":\"UNAUTHORIZED\"}".getBytes(StandardCharsets.UTF_8);
            return exchange.getResponse().writeWith(Mono.just(
                    exchange.getResponse().bufferFactory().wrap(body)));
        }
    }
}
