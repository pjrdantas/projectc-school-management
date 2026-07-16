package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.MonolithDashboardAlertaReadPort;
import reactor.core.publisher.Mono;

@Component
public class MonolithDashboardAlertaReadClient extends AbstractDownstreamClientSupport implements MonolithDashboardAlertaReadPort {

    private final WebClient webClient;

    public MonolithDashboardAlertaReadClient(
            @Qualifier("monolithWebClient")
            WebClient monolithWebClient) {
        this.webClient = monolithWebClient;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> consultar(
            String publicoCodigo,
            UUID professorId,
            CatalogReadQuery query) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/api/dashboard/alertas")
                            .queryParam("publicoCodigo", publicoCodigo);
                    if (professorId != null) {
                        builder.queryParam("professorId", professorId);
                    }
                    return builder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para alertas de dashboard"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para alertas de dashboard"));
    }
}
