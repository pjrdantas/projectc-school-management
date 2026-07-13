package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.MonolithAuthSessionPort;
import reactor.core.publisher.Mono;

@Component
public class MonolithAuthSessionClient extends AbstractDownstreamClientSupport implements MonolithAuthSessionPort {

    private final WebClient webClient;

    public MonolithAuthSessionClient(@Qualifier("monolithWebClient") WebClient monolithWebClient) {
        this.webClient = monolithWebClient;
    }

    @Override
    public Mono<ResponseEntity<String>> listarEscolas(CatalogReadQuery query) {
        return webClient.get()
                .uri("/internal/auth/escolas")
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .exchangeToMono(response -> handle(response, "Monolito rejeitou a listagem interna de escolas"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel"));
    }

    @Override
    public Mono<ResponseEntity<String>> selecionarEscolaAtiva(String requestBody, CatalogReadQuery query) {
        return webClient.post()
                .uri("/internal/auth/escola-ativa")
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchangeToMono(response -> handle(response, "Monolito rejeitou a selecao interna de escola ativa"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel"));
    }
}
