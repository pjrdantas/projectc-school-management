package br.com.escola.bff.infra.webclient;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.DashboardIndicadorSnapshotReadPort;
import br.com.escola.bff.infra.config.DashboardQueryServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class DashboardIndicadorSnapshotReadClient extends AbstractDownstreamClientSupport
        implements DashboardIndicadorSnapshotReadPort {

    private final WebClient webClient;
    private final DashboardQueryServiceClientProperties properties;

    public DashboardIndicadorSnapshotReadClient(
            @Qualifier("dashboardQueryServiceWebClient")
            WebClient dashboardQueryServiceWebClient,
            DashboardQueryServiceClientProperties properties) {
        this.webClient = dashboardQueryServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarPorPublicoCodigo(
            String publicoCodigo,
            LocalDate referenciaData,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/internal/v1/dashboard/snapshots/publicos/{publicoCodigo}");
                    if (referenciaData != null) {
                        builder.queryParam("referenciaData", referenciaData);
                    }
                    return builder.build(publicoCodigo);
                })
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Dashboard query service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Dashboard query service indisponivel"));
    }
}
