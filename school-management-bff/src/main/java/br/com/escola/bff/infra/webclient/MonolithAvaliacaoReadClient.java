package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.MonolithAvaliacaoReadPort;
import reactor.core.publisher.Mono;

@Component
public class MonolithAvaliacaoReadClient extends AbstractDownstreamClientSupport implements MonolithAvaliacaoReadPort {

    private final WebClient webClient;

    public MonolithAvaliacaoReadClient(
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
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para avaliacoes"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para avaliacoes"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> buscarPorId(
            UUID avaliacaoId,
            CatalogReadQuery query) {
        return webClient.get()
                .uri("/api/avaliacoes/{id}", avaliacaoId)
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para avaliacao"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para avaliacao"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarNotasPorAvaliacao(
            UUID avaliacaoId,
            CatalogReadQuery query) {
        return webClient.get()
                .uri("/api/avaliacoes/{id}/notas", avaliacaoId)
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para notas por avaliacao"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para notas por avaliacao"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarNotasPorMatricula(
            UUID matriculaId,
            CatalogReadQuery query) {
        return webClient.get()
                .uri("/api/matriculas/{matriculaId}/notas", matriculaId)
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para notas por matricula"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para notas por matricula"));
    }

    private java.net.URI montarUriListagem(
            UriBuilder uriBuilder,
            UUID professorTurmaDisciplinaId,
            UUID turmaId) {
        UriBuilder builder = uriBuilder.path("/api/avaliacoes");
        if (professorTurmaDisciplinaId != null) {
            builder.queryParam("professorTurmaDisciplinaId", professorTurmaDisciplinaId);
        }
        if (turmaId != null) {
            builder.queryParam("turmaId", turmaId);
        }
        return builder.build();
    }
}
