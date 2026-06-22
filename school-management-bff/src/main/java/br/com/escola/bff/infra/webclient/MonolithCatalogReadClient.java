package br.com.escola.bff.infra.webclient;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.MonolithCatalogReadPort;
import br.com.escola.bff.infra.config.MonolithClientProperties;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import reactor.core.publisher.Mono;

@Component
public class MonolithCatalogReadClient extends AbstractDownstreamClientSupport implements MonolithCatalogReadPort {

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;
    private final MonolithClientProperties properties;

    public MonolithCatalogReadClient(
            @Qualifier("monolithWebClient")
            WebClient monolithWebClient,
            CircuitBreaker monolithCircuitBreaker,
            MonolithClientProperties properties) {
        this.webClient = monolithWebClient;
        this.circuitBreaker = monolithCircuitBreaker;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> get(String path, CatalogReadQuery query) {
        return webClient.get()
                .uri(path)
                .header("Authorization", query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito retornou erro interno"))
                .timeout(properties.responseTimeout())
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorMap(CallNotPermittedException.class,
                        error -> new br.com.escola.bff.application.exception.DownstreamUnavailableException(
                                "Circuit breaker do monolito esta aberto", error))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel"));
    }
}
