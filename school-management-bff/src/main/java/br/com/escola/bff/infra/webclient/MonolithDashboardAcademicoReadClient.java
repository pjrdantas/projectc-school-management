package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.MonolithDashboardAcademicoReadPort;
import reactor.core.publisher.Mono;

@Component
public class MonolithDashboardAcademicoReadClient extends AbstractDownstreamClientSupport implements MonolithDashboardAcademicoReadPort {

    private final WebClient webClient;

    public MonolithDashboardAcademicoReadClient(
            @Qualifier("monolithWebClient")
            WebClient monolithWebClient) {
        this.webClient = monolithWebClient;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> consultar(CatalogReadQuery query) {
        return webClient.get()
                .uri("/api/dashboard/academico")
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para dashboard academico"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para dashboard academico"));
    }
}
