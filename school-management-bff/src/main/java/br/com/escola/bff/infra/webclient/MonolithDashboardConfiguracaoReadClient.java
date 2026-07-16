package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.MonolithDashboardConfiguracaoReadPort;
import reactor.core.publisher.Mono;

@Component
public class MonolithDashboardConfiguracaoReadClient extends AbstractDownstreamClientSupport
        implements MonolithDashboardConfiguracaoReadPort {

    private final WebClient webClient;

    public MonolithDashboardConfiguracaoReadClient(
            @Qualifier("monolithWebClient")
            WebClient monolithWebClient) {
        this.webClient = monolithWebClient;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarDashboards(
            UUID publicoDashboardId,
            String publicoCodigo,
            CatalogReadQuery query) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/api/dashboard/configuracoes/dashboards");
                    if (publicoDashboardId != null) {
                        builder.queryParam("publicoDashboardId", publicoDashboardId);
                    }
                    if (publicoCodigo != null) {
                        builder.queryParam("publicoCodigo", publicoCodigo);
                    }
                    return builder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para dashboards de configuracao"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para dashboards de configuracao"));
    }
}
