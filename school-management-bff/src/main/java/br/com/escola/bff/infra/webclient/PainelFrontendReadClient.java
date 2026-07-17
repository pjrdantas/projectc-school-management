package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.PainelFrontendReadPort;
import br.com.escola.bff.infra.config.PainelQueryServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class PainelFrontendReadClient extends AbstractDownstreamClientSupport implements PainelFrontendReadPort {

    private final WebClient webClient;
    private final PainelQueryServiceClientProperties properties;

    public PainelFrontendReadClient(
            @Qualifier("dashboardQueryServiceWebClient")
            WebClient dashboardQueryServiceWebClient,
            PainelQueryServiceClientProperties properties) {
        this.webClient = dashboardQueryServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> consultar(
            String publicoCodigo,
            UUID usuarioId,
            UUID professorId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/internal/v1/dashboard/frontend")
                            .queryParam("publicoCodigo", publicoCodigo);
                    if (usuarioId != null) {
                        builder.queryParam("usuarioId", usuarioId);
                    }
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
                .exchangeToMono(response -> handle(response, "Painel query service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Painel query service indisponivel"));
    }
}

