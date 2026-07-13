package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.CriarPlanejamentoIaConteudoUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class PlanejamentoIaConteudoWriteControllerTest {

    @Test
    void deveExporContratoCompativelNaGeracaoOficialDeConteudoPlanejamentoIa() {
        UUID planejamentoId = UUID.fromString("00000000-0000-0000-0000-000000000711");
        CriarPlanejamentoIaConteudoUseCase useCase =
                (authorization, correlationId, requestedPlanejamentoId, requestBody) -> Mono.just(ResponseEntity.status(201)
                        .body("""
                                {
                                  "id":"00000000-0000-0000-0000-000000000811",
                                  "planejamentoBimestralId":"00000000-0000-0000-0000-000000000711",
                                  "titulo":"Lista de fracoes",
                                  "tipoConteudo":"ATIVIDADE",
                                  "status":"GERADO"
                                }
                                """));

        WebTestClient client = WebTestClient.bindToController(new PlanejamentoIaConteudoWriteController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.post().uri("/api/planejamentos-bimestrais/{planejamentoId}/ia/conteudos", planejamentoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-planejamento-conteudo-write-1")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue("""
                        {"promptProfessor":"Gerar atividade","tipoConteudo":"ATIVIDADE","titulo":"Lista de fracoes","reutilizavel":true}
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.planejamentoBimestralId").isEqualTo(planejamentoId.toString())
                .jsonPath("$.status").isEqualTo("GERADO");
    }
}
