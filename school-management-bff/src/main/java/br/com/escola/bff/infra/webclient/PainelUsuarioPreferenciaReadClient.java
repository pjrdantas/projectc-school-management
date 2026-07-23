package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.PainelUsuarioPreferenciaReadPort;
import br.com.escola.bff.infra.config.PainelQueryServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class PainelUsuarioPreferenciaReadClient extends AbstractDownstreamClientSupport
        implements PainelUsuarioPreferenciaReadPort {

    private final WebClient webClient;
    private final PainelQueryServiceClientProperties properties;

    public PainelUsuarioPreferenciaReadClient(
            @Qualifier("dashboardQueryServiceWebClient") WebClient dashboardQueryServiceWebClient,
            PainelQueryServiceClientProperties properties) {
        this.webClient = dashboardQueryServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listar(
            UUID usuarioId, UUID painelId, CatalogReadQuery query, AuthSessionContext context) {
        var uri = UriComponentsBuilder.fromPath("/internal/v1/dashboard/usuarios/{usuarioId}/configuracoes");
        if (painelId != null) {
            uri.queryParam("painelId", painelId);
        }
        return webClient.get().uri(uri.buildAndExpand(usuarioId).toUriString())
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
