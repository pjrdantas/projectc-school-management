package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.DashboardAlertaReadPort;
import br.com.escola.bff.infra.config.DashboardQueryServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class DashboardAlertaReadClient extends AbstractDownstreamClientSupport implements DashboardAlertaReadPort {

    private final WebClient webClient;
    private final DashboardQueryServiceClientProperties properties;

    public DashboardAlertaReadClient(
            @Qualifier("dashboardQueryServiceWebClient")
            WebClient dashboardQueryServiceWebClient,
            DashboardQueryServiceClientProperties properties) {
        this.webClient = dashboardQueryServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> consultar(
            String publicoCodigo,
            UUID professorId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/internal/v1/dashboard/alertas")
                            .queryParam("publicoCodigo", publicoCodigo);
                    if (professorId != null) {
                        builder.queryParam("professorId", professorId);
                    }
                    return builder.build();
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
