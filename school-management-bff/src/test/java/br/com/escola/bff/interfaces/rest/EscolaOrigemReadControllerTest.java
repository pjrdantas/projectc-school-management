package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarEscolaOrigemUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class EscolaOrigemReadControllerTest {

    @Test
    void deveExporContratoCompativelNaBuscaDeEscolaOrigemPorId() {
        UUID escolaOrigemId = UUID.fromString("00000000-0000-0000-0000-000000000081");
        ConsultarEscolaOrigemUseCase useCase = (authorization, correlationId, requestedEscolaOrigemId) -> Mono.just(
                ResponseEntity.ok("""
                        {
                          "id":"00000000-0000-0000-0000-000000000081",
                          "nomeEscola":"Escola Origem Oficial",
                          "codigoInep":"12345678",
                          "cidade":"Recife",
                          "uf":"PE"
                        }
                        """));

        WebTestClient client = WebTestClient.bindToController(new EscolaOrigemReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/escolas-origem/{escolaOrigemId}", escolaOrigemId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-escola-origem-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(escolaOrigemId.toString())
                .jsonPath("$.nomeEscola").isEqualTo("Escola Origem Oficial");
    }
}
