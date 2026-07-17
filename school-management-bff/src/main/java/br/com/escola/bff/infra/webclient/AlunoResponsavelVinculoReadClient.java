package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AlunoResponsavelVinculoReadPort;
import br.com.escola.bff.infra.config.ResponsavelCatalogoServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class AlunoResponsavelVinculoReadClient extends AbstractDownstreamClientSupport
        implements AlunoResponsavelVinculoReadPort {

    private final WebClient webClient;
    private final ResponsavelCatalogoServiceClientProperties properties;

    public AlunoResponsavelVinculoReadClient(
            @Qualifier("responsiblesServiceWebClient")
            WebClient responsiblesServiceWebClient,
            ResponsavelCatalogoServiceClientProperties properties) {
        this.webClient = responsiblesServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarResponsaveisPorAluno(
            UUID alunoId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/alunos/{alunoId}/responsaveis", alunoId)
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

