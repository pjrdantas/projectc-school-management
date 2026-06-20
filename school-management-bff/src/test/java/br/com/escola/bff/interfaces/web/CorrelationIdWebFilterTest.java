package br.com.escola.bff.interfaces.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import br.com.escola.bff.application.context.TrustedHeaders;

class CorrelationIdWebFilterTest {

    private final WebFilter filter = new CorrelationIdWebFilter();

    @Test
    void devePreservarCorrelationIdRecebido() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health")
                        .header(TrustedHeaders.CORRELATION_ID, "corr-123"));
        WebFilterChain chain = ignored -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(exchange.getResponse().getHeaders()
                .getFirst(TrustedHeaders.CORRELATION_ID)).isEqualTo("corr-123");
    }

    @Test
    void deveGerarCorrelationIdQuandoAusente() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health"));

        StepVerifier.create(filter.filter(exchange, ignored -> Mono.empty())).verifyComplete();

        assertThat(exchange.getResponse().getHeaders()
                .getFirst(TrustedHeaders.CORRELATION_ID)).isNotBlank();
    }
}
