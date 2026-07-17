package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.LegacyAulaReadPort;
import reactor.core.publisher.Mono;

@Component
public class LegacyAulaReadClient extends AbstractDownstreamClientSupport implements LegacyAulaReadPort {

    private final WebClient webClient;

    public LegacyAulaReadClient(
            @Qualifier("monolithWebClient")
            WebClient monolithWebClient) {
        this.webClient = monolithWebClient;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listar(
            UUID professorTurmaDisciplinaId,
            UUID turmaId,
            CatalogReadQuery query) {
        return webClient.get()
                .uri(uriBuilder -> montarUriListagem(uriBuilder, professorTurmaDisciplinaId, turmaId))
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para aulas"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para aulas"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> buscarPorId(
            UUID aulaId,
            CatalogReadQuery query) {
        return webClient.get()
                .uri("/api/aulas/{id}", aulaId)
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para aula"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para aula"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarFrequenciaProfessor(
            UUID aulaId,
            CatalogReadQuery query) {
        return webClient.get()
                .uri("/api/aulas/{id}/frequencia-professor", aulaId)
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para frequencia de professor"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para frequencia de professor"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarFrequenciasAlunos(
            UUID aulaId,
            CatalogReadQuery query) {
        return webClient.get()
                .uri("/api/aulas/{id}/frequencias-alunos", aulaId)
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para frequencias de alunos"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para frequencias de alunos"));
    }

    private java.net.URI montarUriListagem(
            UriBuilder uriBuilder,
            UUID professorTurmaDisciplinaId,
            UUID turmaId) {
        UriBuilder builder = uriBuilder.path("/api/aulas");
        if (professorTurmaDisciplinaId != null) {
            builder.queryParam("professorTurmaDisciplinaId", professorTurmaDisciplinaId);
        }
        if (turmaId != null) {
            builder.queryParam("turmaId", turmaId);
        }
        return builder.build();
    }
}

