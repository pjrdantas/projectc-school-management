package br.com.escola.bff.infra.webclient;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.LegacyDiarioClasseReadPort;
import reactor.core.publisher.Mono;

@Component
public class LegacyDiarioClasseReadClient extends AbstractDownstreamClientSupport
        implements LegacyDiarioClasseReadPort {

    private final WebClient webClient;

    public LegacyDiarioClasseReadClient(
            @Qualifier("monolithWebClient")
            WebClient monolithWebClient) {
        this.webClient = monolithWebClient;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> carregar(
            UUID professorId,
            UUID turmaId,
            UUID disciplinaId,
            Integer anoLetivo,
            Integer mes,
            LocalDate dataReferencia,
            CatalogReadQuery query) {
        String uri = UriComponentsBuilder.fromPath("/api/diarios-classe")
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
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para diario de classe"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para diario de classe"));
    }
}

