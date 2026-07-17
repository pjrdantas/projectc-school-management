package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.PainelConfiguracaoReadPort;
import br.com.escola.bff.infra.config.PainelQueryServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class PainelConfiguracaoReadClient extends AbstractDownstreamClientSupport
        implements PainelConfiguracaoReadPort {

    private final WebClient webClient;
    private final PainelQueryServiceClientProperties properties;

    public PainelConfiguracaoReadClient(
            @Qualifier("dashboardQueryServiceWebClient")
            WebClient dashboardQueryServiceWebClient,
            PainelQueryServiceClientProperties properties) {
        this.webClient = dashboardQueryServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarPainels(
            UUID publicoPainelId,
            String publicoCodigo,
            CatalogReadQuery query,
            AuthSessionContext context) {
        var uriBuilder = UriComponentsBuilder.fromPath("/internal/v1/dashboard/configuracoes/dashboards");
        if (publicoPainelId != null) {
            uriBuilder.queryParam("publicoPainelId", publicoPainelId);
        }
        if (StringUtils.hasText(publicoCodigo)) {
            uriBuilder.queryParam("publicoCodigo", publicoCodigo);
        }
        return webClient.get()
                .uri(uriBuilder.build(true).toUriString())
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

