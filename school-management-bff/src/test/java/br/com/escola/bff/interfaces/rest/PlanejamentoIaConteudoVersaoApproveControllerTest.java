package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.AprovarPlanejamentoIaConteudoVersaoUseCase;
import br.com.escola.bff.application.usecase.CriarPlanejamentoIaConteudoUseCase;
import br.com.escola.bff.application.usecase.CriarPlanejamentoIaConteudoVersaoUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class PlanejamentoIaConteudoVersaoApproveControllerTest {

    @Test
    void deveExporContratoCompativelNaAprovacaoOficialDeVersaoConteudoIa() {
        UUID conteudoId = UUID.fromString("00000000-0000-0000-0000-000000001011");
        CriarPlanejamentoIaConteudoUseCase criarConteudoUseCase =
                (authorization, correlationId, planejamentoId, requestBody) -> Mono.error(new UnsupportedOperationException());
        CriarPlanejamentoIaConteudoVersaoUseCase criarVersaoUseCase =
                (authorization, correlationId, requestedConteudoId, requestBody) -> Mono.error(new UnsupportedOperationException());
        AprovarPlanejamentoIaConteudoVersaoUseCase aprovarVersaoUseCase =
                (authorization, correlationId, requestedConteudoId, requestBody) -> Mono.just(ResponseEntity.ok("""
                        {
                          "id":"00000000-0000-0000-0000-000000001011",
                          "versao":2,
                          "status":"APROVADO",
                          "aprovadoPeloProfessor":true
                        }
                        """));

        WebTestClient client = WebTestClient.bindToController(
                        new PlanejamentoIaConteudoWriteController(
                                criarConteudoUseCase,
                                criarVersaoUseCase,
                                aprovarVersaoUseCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.patch().uri("/api/ia/conteudos/{conteudoId}/aprovar-versao", conteudoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-planejamento-versao-approve-1")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue("""
                        {"numeroVersao":2,"publicarBiblioteca":false}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.versao").isEqualTo(2)
                .jsonPath("$.status").isEqualTo("APROVADO")
                .jsonPath("$.aprovadoPeloProfessor").isEqualTo(true);
    }
}
