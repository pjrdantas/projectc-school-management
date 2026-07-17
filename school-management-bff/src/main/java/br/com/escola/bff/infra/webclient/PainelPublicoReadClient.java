package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.PainelPublicoReadPort;
import br.com.escola.bff.infra.config.PainelQueryServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class PainelPublicoReadClient extends AbstractDownstreamClientSupport implements PainelPublicoReadPort {

    private final WebClient webClient;
    private final PainelQueryServiceClientProperties properties;

    public PainelPublicoReadClient(
            @Qualifier("dashboardQueryServiceWebClient")
            WebClient dashboardQueryServiceWebClient,
            PainelQueryServiceClientProperties properties) {
        this.webClient = dashboardQueryServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarPublicos(
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/dashboard/configuracoes/publicos")
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

