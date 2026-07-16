package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.MonolithDashboardProfessorReadPort;
import reactor.core.publisher.Mono;

@Component
public class MonolithDashboardProfessorReadClient extends AbstractDownstreamClientSupport implements MonolithDashboardProfessorReadPort {

    private final WebClient webClient;

    public MonolithDashboardProfessorReadClient(
            @Qualifier("monolithWebClient")
            WebClient monolithWebClient) {
        this.webClient = monolithWebClient;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> consultar(UUID professorId, CatalogReadQuery query) {
        return webClient.get()
                .uri("/api/dashboard/professores/{professorId}", professorId)
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para dashboard professor"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para dashboard professor"));
    }
}
