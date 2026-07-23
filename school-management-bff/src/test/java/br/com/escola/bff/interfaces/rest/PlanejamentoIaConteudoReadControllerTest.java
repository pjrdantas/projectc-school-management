package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoIaConteudoUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class PlanejamentoIaConteudoReadControllerTest {

    @Test
    void deveExporContratoCompativelNaListagemOficialDeConteudosPlanejamentoIa() {
        UUID planejamentoId = UUID.fromString("00000000-0000-0000-0000-000000000711");
        ConsultarPlanejamentoIaConteudoUseCase useCase =
                (authorization, correlationId, requestedPlanejamentoId) -> Mono.just(ResponseEntity.ok("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000811",
                          "planejamentoBimestralId":"00000000-0000-0000-0000-000000000711",
                          "interacaoId":"00000000-0000-0000-0000-000000000611",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao",
                          "titulo":"Lista de fracoes",
                          "conteudo":"Conteudo gerado",
                          "versao":1,
                          "hashConteudo":"abc123",
                          "aprovadoPeloProfessor":false,
                          "reutilizavel":true,
                          "ativo":true,
                          "status":"GERADO",
                          "statusDescricao":"Gerado",
                          "tipoConteudo":"ATIVIDADE",
                          "tipoConteudoDescricao":"Atividade",
                          "createdAt":"2026-07-13T11:10:00",
                          "updatedAt":"2026-07-13T11:10:00"
                        }]
                        """));

        WebTestClient client = WebTestClient.bindToController(new PlanejamentoIaConteudoReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/planejamentos-bimestrais/{planejamentoId}/ia/conteudos", planejamentoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-planejamento-conteudo-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].planejamentoBimestralId").isEqualTo(planejamentoId.toString())
                .jsonPath("$[0].tipoConteudo").isEqualTo("ATIVIDADE");
    }
}
