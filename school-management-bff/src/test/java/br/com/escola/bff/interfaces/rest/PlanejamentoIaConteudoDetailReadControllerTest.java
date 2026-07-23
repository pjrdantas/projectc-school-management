package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoIaConteudoDetailUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class PlanejamentoIaConteudoDetailReadControllerTest {

    @Test
    void deveExporContratoCompativelNaBuscaOficialDeConteudoPlanejamentoIa() {
        UUID conteudoId = UUID.fromString("00000000-0000-0000-0000-000000000911");
        ConsultarPlanejamentoIaConteudoDetailUseCase useCase =
                (authorization, correlationId, requestedConteudoId) -> Mono.just(ResponseEntity.ok("""
                        {
                          "id":"00000000-0000-0000-0000-000000000911",
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
                        }
                        """));

        WebTestClient client = WebTestClient.bindToController(new PlanejamentoIaConteudoDetailReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/ia/conteudos/{conteudoId}", conteudoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-planejamento-conteudo-detail-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(conteudoId.toString())
                .jsonPath("$.tipoConteudo").isEqualTo("ATIVIDADE");
    }
}
