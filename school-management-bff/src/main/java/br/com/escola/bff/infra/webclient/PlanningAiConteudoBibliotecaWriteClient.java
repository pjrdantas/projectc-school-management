package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.PlanningAiConteudoBibliotecaWritePort;
import br.com.escola.bff.infra.config.PlanningAiServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class PlanningAiConteudoBibliotecaWriteClient extends AbstractDownstreamClientSupport
        implements PlanningAiConteudoBibliotecaWritePort {

    private final WebClient webClient;
    private final PlanningAiServiceClientProperties properties;

    public PlanningAiConteudoBibliotecaWriteClient(
            @Qualifier("planningAiServiceWebClient")
            WebClient planningAiServiceWebClient,
            PlanningAiServiceClientProperties properties) {
        this.webClient = planningAiServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> publicarBiblioteca(
            UUID conteudoId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        String uri = "/internal/v1/ia/conteudos/" + conteudoId + "/publicar-biblioteca";
        return webClient.post()
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
