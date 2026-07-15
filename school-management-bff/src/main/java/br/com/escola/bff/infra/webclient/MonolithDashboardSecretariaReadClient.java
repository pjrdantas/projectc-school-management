package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.MonolithDashboardSecretariaReadPort;
import reactor.core.publisher.Mono;

@Component
public class MonolithDashboardSecretariaReadClient extends AbstractDownstreamClientSupport implements MonolithDashboardSecretariaReadPort {

    private final WebClient webClient;

    public MonolithDashboardSecretariaReadClient(
            @Qualifier("monolithWebClient")
            WebClient monolithWebClient) {
        this.webClient = monolithWebClient;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> consultar(CatalogReadQuery query) {
        return webClient.get()
                .uri("/api/dashboard/secretaria")
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para dashboard secretaria"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para dashboard secretaria"));
    }
}
