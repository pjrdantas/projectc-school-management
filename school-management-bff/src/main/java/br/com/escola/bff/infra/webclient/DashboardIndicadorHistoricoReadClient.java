package br.com.escola.bff.infra.webclient;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.DashboardIndicadorHistoricoReadPort;
import br.com.escola.bff.infra.config.DashboardQueryServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class DashboardIndicadorHistoricoReadClient extends AbstractDownstreamClientSupport
        implements DashboardIndicadorHistoricoReadPort {

    private final WebClient webClient;
    private final DashboardQueryServiceClientProperties properties;

    public DashboardIndicadorHistoricoReadClient(
            @Qualifier("dashboardQueryServiceWebClient")
            WebClient dashboardQueryServiceWebClient,
            DashboardQueryServiceClientProperties properties) {
        this.webClient = dashboardQueryServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> consultarHistorico(
            String publicoCodigo,
            String codigoIndicador,
            LocalDate dataInicio,
            LocalDate dataFim,
            UUID professorId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/internal/v1/dashboard/snapshots/historico/publicos/{publicoCodigo}");
                    if (codigoIndicador != null) {
                        builder.queryParam("codigoIndicador", codigoIndicador);
                    }
                    if (dataInicio != null) {
                        builder.queryParam("dataInicio", dataInicio);
                    }
                    if (dataFim != null) {
                        builder.queryParam("dataFim", dataFim);
                    }
                    if (professorId != null) {
                        builder.queryParam("professorId", professorId);
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
