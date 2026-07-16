package br.com.escola.bff.infra.webclient;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.MonolithDashboardIndicadorSnapshotReadPort;
import reactor.core.publisher.Mono;

@Component
public class MonolithDashboardIndicadorSnapshotReadClient extends AbstractDownstreamClientSupport
        implements MonolithDashboardIndicadorSnapshotReadPort {

    private final WebClient webClient;

    public MonolithDashboardIndicadorSnapshotReadClient(
            @Qualifier("monolithWebClient")
            WebClient monolithWebClient) {
        this.webClient = monolithWebClient;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarPorPublicoCodigo(
            String publicoCodigo,
            LocalDate referenciaData,
            CatalogReadQuery query) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/api/dashboard/snapshots/publicos/{publicoCodigo}");
                    if (referenciaData != null) {
                        builder.queryParam("referenciaData", referenciaData);
                    }
                    return builder.build(publicoCodigo);
                })
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para snapshots de dashboard"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para snapshots de dashboard"));
    }
}
