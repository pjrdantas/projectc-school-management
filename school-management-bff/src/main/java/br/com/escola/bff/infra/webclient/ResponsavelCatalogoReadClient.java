package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.ResponsavelCatalogoReadPort;
import br.com.escola.bff.infra.config.ResponsavelCatalogoServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class ResponsavelCatalogoReadClient extends AbstractDownstreamClientSupport implements ResponsavelCatalogoReadPort {

    private final WebClient webClient;
    private final ResponsavelCatalogoServiceClientProperties properties;

    public ResponsavelCatalogoReadClient(
            @Qualifier("responsiblesServiceWebClient")
            WebClient responsiblesServiceWebClient,
            ResponsavelCatalogoServiceClientProperties properties) {
        this.webClient = responsiblesServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarResponsaveis(
            String nome,
            String cpf,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/internal/v1/responsaveis");
                    if (nome != null) {
                        builder.queryParam("nome", nome);
                    }
                    if (cpf != null) {
                        builder.queryParam("cpf", cpf);
                    }
                    return builder.build();
                })
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "ResponsavelCatalogo service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "ResponsavelCatalogo service indisponivel"));
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
                .exchangeToMono(response -> handle(response, "ResponsavelCatalogo service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "ResponsavelCatalogo service indisponivel"));
    }
}

