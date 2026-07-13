package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoIaInteracaoUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class PlanejamentoIaInteracaoReadControllerTest {

    @Test
    void deveExporContratoCompativelNaListagemOficialDeInteracoesPlanejamentoIa() {
        UUID planejamentoId = UUID.fromString("00000000-0000-0000-0000-000000000511");
        ConsultarPlanejamentoIaInteracaoUseCase useCase =
                (authorization, correlationId, requestedPlanejamentoId) -> Mono.just(ResponseEntity.ok("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000611",
                          "planejamentoBimestralId":"00000000-0000-0000-0000-000000000511",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao",
                          "promptProfessor":"Monte uma atividade sobre fracoes",
                          "respostaIA":"Sugestao de atividade",
                          "modeloIA":"gpt-4.1",
                          "tokensEntrada":120,
                          "tokensSaida":340,
                          "custoEstimado":1.25,
                          "createdAt":"2026-07-13T11:00:00"
                        }]
                        """));

        WebTestClient client = WebTestClient.bindToController(new PlanejamentoIaInteracaoReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/planejamentos-bimestrais/{planejamentoId}/ia/interacoes", planejamentoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-planejamento-ia-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].planejamentoBimestralId").isEqualTo(planejamentoId.toString())
                .jsonPath("$[0].modeloIA").isEqualTo("gpt-4.1");
    }
}
