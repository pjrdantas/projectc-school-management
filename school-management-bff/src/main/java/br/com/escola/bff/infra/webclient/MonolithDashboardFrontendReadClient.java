package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.MonolithDashboardFrontendReadPort;
import reactor.core.publisher.Mono;

@Component
public class MonolithDashboardFrontendReadClient extends AbstractDownstreamClientSupport
        implements MonolithDashboardFrontendReadPort {

    private final WebClient webClient;

    public MonolithDashboardFrontendReadClient(@Qualifier("monolithWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> consultar(
            String publicoCodigo,
            UUID usuarioId,
            UUID professorId,
            CatalogReadQuery query) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/api/dashboard/frontend")
                            .queryParam("publicoCodigo", publicoCodigo);
                    if (usuarioId != null) {
                        builder.queryParam("usuarioId", usuarioId);
                    }
                    if (professorId != null) {
                        builder.queryParam("professorId", professorId);
                    }
                    return builder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para dashboard frontend"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para dashboard frontend"));
    }
}
