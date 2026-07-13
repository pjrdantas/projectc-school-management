package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.PlanningAiBibliotecaReadPort;
import br.com.escola.bff.infra.config.PlanningAiServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class PlanningAiBibliotecaReadClient extends AbstractDownstreamClientSupport
        implements PlanningAiBibliotecaReadPort {

    private final WebClient webClient;
    private final PlanningAiServiceClientProperties properties;

    public PlanningAiBibliotecaReadClient(
            @Qualifier("planningAiServiceWebClient")
            WebClient planningAiServiceWebClient,
            PlanningAiServiceClientProperties properties) {
        this.webClient = planningAiServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarBiblioteca(
            UUID professorId,
            UUID disciplinaId,
            String tipoConteudo,
            String tema,
            CatalogReadQuery query,
            AuthSessionContext context) {
        String uri = UriComponentsBuilder.fromPath("/internal/v1/biblioteca-conteudos-pedagogicos")
                .queryParamIfPresent("professorId", java.util.Optional.ofNullable(professorId))
                .queryParamIfPresent("disciplinaId", java.util.Optional.ofNullable(disciplinaId))
                .queryParamIfPresent("tipoConteudo", java.util.Optional.ofNullable(tipoConteudo))
                .queryParamIfPresent("tema", java.util.Optional.ofNullable(tema))
                .build()
                .toUriString();
        return webClient.get()
                .uri(uri)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Planning AI service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Planning AI service indisponivel"));
    }
}
