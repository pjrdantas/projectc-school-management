package br.com.escola.bff.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarFuncionarioUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class FuncionarioReadControllerTest {

    @Test
    void deveExporContratoCompativelNaListagemDeFuncionarios() {
        ConsultarFuncionarioUseCase useCase = new ConsultarFuncionarioUseCase() {
            @Override
            public Mono<ResponseEntity<String>> listarFuncionarios(String authorization, String correlationId) {
                return Mono.just(ResponseEntity.ok("""
                        [{
                          "funcionarioId":"00000000-0000-0000-0000-000000000012",
                          "pessoaId":"00000000-0000-0000-0000-000000000022",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "nomeCompleto":"Carlos Lima",
                          "cargoDescricao":"Secretaria",
                          "ativo":true
                        }]
                        """));
            }

            @Override
            public Mono<ResponseEntity<String>> buscarFuncionarioPorId(
                    String authorization,
                    String correlationId,
                    java.util.UUID funcionarioId) {
                return Mono.error(new UnsupportedOperationException());
            }
        };

        WebTestClient client = WebTestClient.bindToController(new FuncionarioReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/funcionarios")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].nomeCompleto").isEqualTo("Carlos Lima")
                .jsonPath("$[0].cargoDescricao").isEqualTo("Secretaria");
    }

    @Test
    void deveExporContratoCompativelNaBuscaDeFuncionarioPorId() {
        java.util.UUID funcionarioId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000012");
        ConsultarFuncionarioUseCase useCase = new ConsultarFuncionarioUseCase() {
            @Override
            public Mono<ResponseEntity<String>> listarFuncionarios(String authorization, String correlationId) {
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> buscarFuncionarioPorId(
                    String authorization,
                    String correlationId,
                    java.util.UUID requestedFuncionarioId) {
                return Mono.just(ResponseEntity.ok("""
                        {
                          "funcionarioId":"00000000-0000-0000-0000-000000000012",
                          "pessoaId":"00000000-0000-0000-0000-000000000022",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "nomeCompleto":"Carlos Lima",
                          "cargoDescricao":"Secretaria",
                          "ativo":true
                        }
                        """));
            }
        };

        WebTestClient client = WebTestClient.bindToController(new FuncionarioReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/funcionarios/{funcionarioId}", funcionarioId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.funcionarioId").isEqualTo(funcionarioId.toString())
                .jsonPath("$.cargoDescricao").isEqualTo("Secretaria");
    }
}
