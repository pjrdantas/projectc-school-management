package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.DiarioClasseWritePort;
import br.com.escola.bff.infra.config.EnsinoClientProperties;
import reactor.core.publisher.Mono;

@Component
public class DiarioClasseWriteClient extends AbstractDownstreamClientSupport
        implements DiarioClasseWritePort {

    private final WebClient webClient;
    private final EnsinoClientProperties properties;

    public DiarioClasseWriteClient(
            @Qualifier("pedagogicalServiceWebClient") WebClient pedagogicalServiceWebClient,
            EnsinoClientProperties properties) {
        this.webClient = pedagogicalServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> salvar(
            String idDiarioClasse,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.put()
                .uri("/internal/v1/diarios-classe/{idDiarioClasse}", idDiarioClasse)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .bodyValue(requestBody)
                .exchangeToMono(response -> handle(response, "Pedagogical service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Pedagogical service indisponivel"));
    }
}


