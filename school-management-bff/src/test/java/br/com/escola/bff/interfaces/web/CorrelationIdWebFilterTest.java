package br.com.escola.bff.interfaces.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

class CorrelationIdWebFilterTest {

    private final WebFilter filter = new CorrelationIdWebFilter();

    @Test
    void devePreservarCorrelationIdRecebido() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health")
                        .header(CorrelationIdWebFilter.CORRELATION_ID_HEADER, "corr-123"));
        WebFilterChain chain = ignored -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdWebFilter.CORRELATION_ID_HEADER)).isEqualTo("corr-123");
    }

    @Test
    void deveGerarCorrelationIdQuandoAusente() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health"));

        StepVerifier.create(filter.filter(exchange, ignored -> Mono.empty())).verifyComplete();

        assertThat(exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdWebFilter.CORRELATION_ID_HEADER)).isNotBlank();
    }
}
