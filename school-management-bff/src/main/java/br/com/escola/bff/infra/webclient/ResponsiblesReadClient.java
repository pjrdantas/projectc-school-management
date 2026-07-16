package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.ResponsiblesReadPort;
import br.com.escola.bff.infra.config.ResponsiblesServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class ResponsiblesReadClient extends AbstractDownstreamClientSupport implements ResponsiblesReadPort {

    private final WebClient webClient;
    private final ResponsiblesServiceClientProperties properties;

    public ResponsiblesReadClient(
            @Qualifier("responsiblesServiceWebClient")
            WebClient responsiblesServiceWebClient,
            ResponsiblesServiceClientProperties properties) {
        this.webClient = responsiblesServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> buscarResponsavelPorId(
            UUID responsavelId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/responsaveis/{id}", responsavelId)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Responsibles service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Responsibles service indisponivel"));
    }
}
