package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.PlanejamentoIaConteudoVersaoApprovePort;
import br.com.escola.bff.infra.config.PlanejamentoIaServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class PlanejamentoIaConteudoVersaoApproveClient extends AbstractDownstreamClientSupport
        implements PlanejamentoIaConteudoVersaoApprovePort {

    private final WebClient webClient;
    private final PlanejamentoIaServiceClientProperties properties;

    public PlanejamentoIaConteudoVersaoApproveClient(
            @Qualifier("planningAiServiceWebClient")
            WebClient planningAiServiceWebClient,
            PlanejamentoIaServiceClientProperties properties) {
        this.webClient = planningAiServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> aprovarVersao(
            UUID conteudoId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        String uri = "/internal/v1/ia/conteudos/" + conteudoId + "/aprovar-versao";
        return webClient.patch()
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

