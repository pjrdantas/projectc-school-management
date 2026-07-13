package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.CriarPlanejamentoIaConteudoUseCase;
import br.com.escola.bff.application.usecase.CriarPlanejamentoIaConteudoVersaoUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class PlanejamentoIaConteudoVersaoWriteControllerTest {

    @Test
    void deveExporContratoCompativelNaCriacaoOficialDeVersaoConteudoIa() {
        UUID conteudoId = UUID.fromString("00000000-0000-0000-0000-000000001011");
        CriarPlanejamentoIaConteudoUseCase criarConteudoUseCase =
                (authorization, correlationId, planejamentoId, requestBody) -> Mono.error(new UnsupportedOperationException());
        CriarPlanejamentoIaConteudoVersaoUseCase criarVersaoUseCase =
                (authorization, correlationId, requestedConteudoId, requestBody) -> Mono.just(ResponseEntity.status(201)
                        .body("""
                                {
                                  "id":"00000000-0000-0000-0000-000000001111",
                                  "conteudoGeradoId":"00000000-0000-0000-0000-000000001011",
                                  "numeroVersao":2,
                                  "conteudo":"Conteudo revisado",
                                  "motivoAlteracao":"Ajuste do professor"
                                }
                                """));

        WebTestClient client = WebTestClient.bindToController(
                        new PlanejamentoIaConteudoWriteController(criarConteudoUseCase, criarVersaoUseCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.post().uri("/api/ia/conteudos/{conteudoId}/versoes", conteudoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-planejamento-versao-write-1")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue("""
                        {"conteudo":"Conteudo revisado","motivoAlteracao":"Ajuste do professor"}
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.conteudoGeradoId").isEqualTo(conteudoId.toString())
                .jsonPath("$.numeroVersao").isEqualTo(2);
    }
}
