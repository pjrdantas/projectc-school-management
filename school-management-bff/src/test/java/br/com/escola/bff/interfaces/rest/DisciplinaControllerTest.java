package br.com.escola.bff.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.RouteCatalogReadUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class DisciplinaControllerTest {

    @Test
    void deveExporContratoCompativelComMonolito() {
        RouteCatalogReadUseCase useCase = (route, query, pathArgs) -> Mono.just(ResponseEntity.ok("""
                [{
                  "id":"00000000-0000-0000-0000-000000000001",
                  "nome":"Matematica",
                  "cargaHoraria":80,
                  "status":"ATIVA",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola padrao",
                  "createdAt":"2026-06-19T10:00:00"
                }]
                """));
        WebTestClient client = WebTestClient.bindToController(new CatalogoReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/disciplinas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].nome").isEqualTo("Matematica")
                .jsonPath("$[0].escolaId").isEqualTo("00000000-0000-0000-0000-000000000047");
    }
}

