package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.PedagogicalBoletimReadPort;
import br.com.escola.bff.infra.config.PedagogicalServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class PedagogicalBoletimReadClient extends AbstractDownstreamClientSupport
        implements PedagogicalBoletimReadPort {

    private final WebClient webClient;
    private final PedagogicalServiceClientProperties properties;

    public PedagogicalBoletimReadClient(
            @Qualifier("pedagogicalServiceWebClient")
            WebClient pedagogicalServiceWebClient,
            PedagogicalServiceClientProperties properties) {
        this.webClient = pedagogicalServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> consultarBoletimPorMatricula(
            UUID matriculaId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/matriculas/{matriculaId}/boletim", matriculaId)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Pedagogical service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Pedagogical service indisponivel"));
    }
}
