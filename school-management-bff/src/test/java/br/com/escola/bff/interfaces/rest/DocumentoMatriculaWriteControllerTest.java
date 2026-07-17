package br.com.escola.bff.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.CriarEscolaOrigemUseCase;
import br.com.escola.bff.application.usecase.CriarTransferenciaUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class DocumentoMatriculaWriteControllerTest {

    @Test
    void deveExporContratoCompativelNaCriacaoDeEscolaOrigem() {
        CriarEscolaOrigemUseCase criarEscolaOrigemUseCase = (authorization, correlationId, requestBody) ->
                Mono.just(ResponseEntity.status(201).body("""
                        {
                          "id":"00000000-0000-0000-0000-000000000801",
                          "nomeEscola":"Escola Origem BFF"
                        }
                        """));
        CriarTransferenciaUseCase criarTransferenciaUseCase = (authorization, correlationId, requestBody) ->
                Mono.error(new UnsupportedOperationException());

        WebTestClient client = WebTestClient.bindToController(
                        new DocumentoMatriculaWriteController(criarEscolaOrigemUseCase, criarTransferenciaUseCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.post().uri("/api/escolas-origem")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-enrollment-write-1")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue("""
                        {"nomeEscola":"Escola Origem BFF"}
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.nomeEscola").isEqualTo("Escola Origem BFF");
    }

    @Test
    void deveExporContratoCompativelNaCriacaoDeTransferencia() {
        CriarEscolaOrigemUseCase criarEscolaOrigemUseCase = (authorization, correlationId, requestBody) ->
                Mono.error(new UnsupportedOperationException());
        CriarTransferenciaUseCase criarTransferenciaUseCase = (authorization, correlationId, requestBody) ->
                Mono.just(ResponseEntity.status(201).body("""
                        {
                          "id":"00000000-0000-0000-0000-000000000901",
                          "alunoId":"00000000-0000-0000-0000-000000000021",
                          "tipoTransferencia":"ENTRADA"
                        }
                        """));

        WebTestClient client = WebTestClient.bindToController(
                        new DocumentoMatriculaWriteController(criarEscolaOrigemUseCase, criarTransferenciaUseCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.post().uri("/api/transferencias")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-enrollment-write-2")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue("""
                        {"alunoId":"00000000-0000-0000-0000-000000000021","serieOrigem":"5A","anoLetivoOrigem":"2026"}
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.tipoTransferencia").isEqualTo("ENTRADA");
    }
}

