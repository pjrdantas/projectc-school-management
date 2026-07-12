package br.com.escola.bff.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarProfessorUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class ProfessorReadControllerTest {

    @Test
    void deveExporContratoCompativelNaListagemDeProfessores() {
        ConsultarProfessorUseCase useCase = new ConsultarProfessorUseCase() {
            @Override
            public Mono<ResponseEntity<String>> listarProfessores(String authorization, String correlationId) {
                return Mono.just(ResponseEntity.ok("""
                        [{
                          "professorId":"00000000-0000-0000-0000-000000000011",
                          "pessoaId":"00000000-0000-0000-0000-000000000021",
                          "funcionarioId":"00000000-0000-0000-0000-000000000031",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "nomeCompleto":"Ana Souza",
                          "ativo":true
                        }]
                        """));
            }

            @Override
            public Mono<ResponseEntity<String>> buscarProfessorPorId(
                    String authorization,
                    String correlationId,
                    java.util.UUID professorId) {
                return Mono.error(new UnsupportedOperationException());
            }
        };

        WebTestClient client = WebTestClient.bindToController(new ProfessorReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/professores")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].nomeCompleto").isEqualTo("Ana Souza")
                .jsonPath("$[0].escolaId").isEqualTo("00000000-0000-0000-0000-000000000047");
    }

    @Test
    void deveExporContratoCompativelNaBuscaDeProfessorPorId() {
        java.util.UUID professorId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000011");
        ConsultarProfessorUseCase useCase = new ConsultarProfessorUseCase() {
            @Override
            public Mono<ResponseEntity<String>> listarProfessores(String authorization, String correlationId) {
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> buscarProfessorPorId(
                    String authorization,
                    String correlationId,
                    java.util.UUID requestedProfessorId) {
                return Mono.just(ResponseEntity.ok("""
                        {
                          "professorId":"00000000-0000-0000-0000-000000000011",
                          "pessoaId":"00000000-0000-0000-0000-000000000021",
                          "funcionarioId":"00000000-0000-0000-0000-000000000031",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "nomeCompleto":"Ana Souza",
                          "ativo":true
                        }
                        """));
            }
        };

        WebTestClient client = WebTestClient.bindToController(new ProfessorReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/professores/{professorId}", professorId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.professorId").isEqualTo(professorId.toString())
                .jsonPath("$.nomeCompleto").isEqualTo("Ana Souza");
    }
}
