package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.PeopleAlunoResponsavelReadPort;
import br.com.escola.bff.infra.config.PeopleServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class PeopleAlunoResponsavelReadClient extends AbstractDownstreamClientSupport implements PeopleAlunoResponsavelReadPort {

    private final WebClient webClient;
    private final PeopleServiceClientProperties properties;

    public PeopleAlunoResponsavelReadClient(
            @Qualifier("peopleServiceWebClient")
            WebClient peopleServiceWebClient,
            PeopleServiceClientProperties properties) {
        this.webClient = peopleServiceWebClient;
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
                .exchangeToMono(response -> handle(response, "People service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "People service indisponivel"));
    }
}
