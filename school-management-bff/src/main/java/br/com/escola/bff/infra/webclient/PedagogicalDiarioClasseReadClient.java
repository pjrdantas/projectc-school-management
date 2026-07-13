package br.com.escola.bff.infra.webclient;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.PedagogicalDiarioClasseReadPort;
import br.com.escola.bff.infra.config.PedagogicalServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class PedagogicalDiarioClasseReadClient extends AbstractDownstreamClientSupport
        implements PedagogicalDiarioClasseReadPort {

    private final WebClient webClient;
    private final PedagogicalServiceClientProperties properties;

    public PedagogicalDiarioClasseReadClient(
            @Qualifier("pedagogicalServiceWebClient") WebClient pedagogicalServiceWebClient,
            PedagogicalServiceClientProperties properties) {
        this.webClient = pedagogicalServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> carregar(
            UUID professorId,
            UUID turmaId,
            UUID disciplinaId,
            Integer anoLetivo,
            Integer mes,
            LocalDate dataReferencia,
            CatalogReadQuery query,
            AuthSessionContext context) {
        String uri = UriComponentsBuilder.fromPath("/internal/v1/diarios-classe")
                .queryParam("idProfessor", professorId)
                .queryParam("idTurma", turmaId)
                .queryParam("idDisciplina", disciplinaId)
                .queryParam("anoLetivo", anoLetivo)
                .queryParam("mes", mes)
                .queryParam("dataReferencia", dataReferencia)
                .build()
                .toUriString();
        return webClient.get()
                .uri(uri)
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
