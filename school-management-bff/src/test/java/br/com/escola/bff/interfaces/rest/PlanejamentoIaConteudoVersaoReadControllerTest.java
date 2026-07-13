package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoIaConteudoVersaoUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class PlanejamentoIaConteudoVersaoReadControllerTest {

    @Test
    void deveExporContratoCompativelNaListagemOficialDeVersoesConteudoIa() {
        UUID conteudoId = UUID.fromString("00000000-0000-0000-0000-000000001011");
        ConsultarPlanejamentoIaConteudoVersaoUseCase useCase =
                (authorization, correlationId, requestedConteudoId) -> Mono.just(ResponseEntity.ok("""
                        [{
                          "id":"00000000-0000-0000-0000-000000001111",
                          "conteudoGeradoId":"00000000-0000-0000-0000-000000001011",
                          "numeroVersao":2,
                          "conteudo":"Conteudo revisado",
                          "motivoAlteracao":"Ajuste do professor",
                          "createdAt":"2026-07-13T11:20:00"
                        }]
                        """));

        WebTestClient client = WebTestClient.bindToController(new PlanejamentoIaConteudoVersaoReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/ia/conteudos/{conteudoId}/versoes", conteudoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-planejamento-versao-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].conteudoGeradoId").isEqualTo(conteudoId.toString())
                .jsonPath("$[0].numeroVersao").isEqualTo(2);
    }
}
