package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.PlanejamentoIaConteudoWritePort;
import br.com.escola.bff.infra.config.PlanejamentoIaServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class PlanejamentoIaConteudoWriteClient extends AbstractDownstreamClientSupport
        implements PlanejamentoIaConteudoWritePort {

    private final WebClient webClient;
    private final PlanejamentoIaServiceClientProperties properties;

    public PlanejamentoIaConteudoWriteClient(
            @Qualifier("planningAiServiceWebClient")
            WebClient planningAiServiceWebClient,
            PlanejamentoIaServiceClientProperties properties) {
        this.webClient = planningAiServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> criarConteudo(
            UUID planejamentoId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        String uri = "/internal/v1/planejamentos-bimestrais/" + planejamentoId + "/ia/conteudos";
        return webClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .bodyValue(requestBody)
                .exchangeToMono(response -> handle(response, "Planning AI service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Planning AI service indisponivel"));
    }
}

