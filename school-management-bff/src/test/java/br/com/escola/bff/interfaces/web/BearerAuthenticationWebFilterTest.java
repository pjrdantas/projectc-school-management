package br.com.escola.bff.interfaces.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.bff.application.context.TrustedHeaders;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class BearerAuthenticationWebFilterTest {

    private final BearerAuthenticationWebFilter filter =
            new BearerAuthenticationWebFilter(new ObjectMapper().findAndRegisterModules());

    @Test
    void deveRejeitarRotaProtegidaSemBearerToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/turmas/00000000-0000-0000-0000-000000000001/disciplinas")
                        .header(TrustedHeaders.CORRELATION_ID, "corr-1"));

        StepVerifier.create(filter.filter(exchange, ignored -> Mono.empty())).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("UNAUTHORIZED", "corr-1");
    }

    @Test
    void deveRemoverContextoForjadoAntesDeContinuar() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/disciplinas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                        .header("X-Usuario-Id", "forged-user")
                        .header("X-Escola-Id", "forged-school"));
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

        StepVerifier.create(filter.filter(exchange, sanitized -> {
            forwarded.set(sanitized);
            return Mono.empty();
        })).verifyComplete();

        assertThat(forwarded.get().getRequest().getHeaders().getFirst("X-Usuario-Id")).isNull();
        assertThat(forwarded.get().getRequest().getHeaders().getFirst("X-Escola-Id")).isNull();
        assertThat(forwarded.get().getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo("Bearer opaque-token");
    }
}
