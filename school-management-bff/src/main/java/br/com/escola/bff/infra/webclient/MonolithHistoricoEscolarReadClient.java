package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.MonolithHistoricoEscolarReadPort;
import reactor.core.publisher.Mono;

@Component
public class MonolithHistoricoEscolarReadClient extends AbstractDownstreamClientSupport
        implements MonolithHistoricoEscolarReadPort {

    private final WebClient webClient;

    public MonolithHistoricoEscolarReadClient(
            @Qualifier("monolithWebClient")
            WebClient monolithWebClient) {
        this.webClient = monolithWebClient;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> carregarNovo(
            UUID alunoId,
            UUID matriculaId,
            String modo,
            CatalogReadQuery query) {
        String uri = UriComponentsBuilder.fromPath("/api/historicos-escolares/novo")
                .queryParam("idAluno", alunoId)
                .queryParam("idMatricula", matriculaId)
                .queryParam("modo", modo)
                .build()
                .toUriString();
        return webClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para historico escolar"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para historico escolar"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> carregarParaEdicao(
            UUID historicoEscolarId,
            CatalogReadQuery query) {
        return webClient.get()
                .uri("/api/historicos-escolares/{id}/carregamento", historicoEscolarId)
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para historico escolar"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para historico escolar"));
    }
}
