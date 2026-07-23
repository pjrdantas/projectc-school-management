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

            @Override
            public Mono<ResponseEntity<String>> listarAlocacoesPorProfessor(
                    String authorization,
                    String correlationId,
                    java.util.UUID professorId) {
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> listarProfessoresPorTurma(
                    String authorization,
                    String correlationId,
                    java.util.UUID turmaId) {
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> listarFuncionariosElegiveis(String authorization, String correlationId) {
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

            @Override
            public Mono<ResponseEntity<String>> listarAlocacoesPorProfessor(
                    String authorization,
                    String correlationId,
                    java.util.UUID professorId) {
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> listarProfessoresPorTurma(
                    String authorization,
                    String correlationId,
                    java.util.UUID turmaId) {
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> listarFuncionariosElegiveis(String authorization, String correlationId) {
                return Mono.error(new UnsupportedOperationException());
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

    @Test
    void deveExporContratoCompativelNaListagemDeAlocacoesPorProfessor() {
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
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> listarAlocacoesPorProfessor(
                    String authorization,
                    String correlationId,
                    java.util.UUID requestedProfessorId) {
                return Mono.just(ResponseEntity.ok("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000041",
                          "professorId":"00000000-0000-0000-0000-000000000011",
                          "professorNome":"Ana Souza",
                          "turmaDisciplinaId":"00000000-0000-0000-0000-000000000051",
                          "turmaId":"00000000-0000-0000-0000-000000000061",
                          "turmaNome":"1A",
                          "disciplinaId":"00000000-0000-0000-0000-000000000071",
                          "disciplinaNome":"Matematica",
                          "ativo":true
                        }]
                        """));
            }

            @Override
            public Mono<ResponseEntity<String>> listarProfessoresPorTurma(
                    String authorization,
                    String correlationId,
                    java.util.UUID turmaId) {
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> listarFuncionariosElegiveis(String authorization, String correlationId) {
                return Mono.error(new UnsupportedOperationException());
            }
        };

        WebTestClient client = WebTestClient.bindToController(new ProfessorReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/professores/{professorId}/turmas-disciplinas", professorId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-3")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].professorId").isEqualTo(professorId.toString())
                .jsonPath("$[0].disciplinaNome").isEqualTo("Matematica");
    }

    @Test
    void deveExporContratoCompativelNaListagemDeProfessoresPorTurma() {
        java.util.UUID turmaId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000061");
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
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> listarAlocacoesPorProfessor(
                    String authorization,
                    String correlationId,
                    java.util.UUID professorId) {
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> listarProfessoresPorTurma(
                    String authorization,
                    String correlationId,
                    java.util.UUID requestedTurmaId) {
                return Mono.just(ResponseEntity.ok("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000041",
                          "professorId":"00000000-0000-0000-0000-000000000011",
                          "professorNome":"Ana Souza",
                          "turmaDisciplinaId":"00000000-0000-0000-0000-000000000051",
                          "turmaId":"00000000-0000-0000-0000-000000000061",
                          "turmaNome":"1A",
                          "disciplinaId":"00000000-0000-0000-0000-000000000071",
                          "disciplinaNome":"Matematica",
                          "ativo":true
                        }]
                        """));
            }

            @Override
            public Mono<ResponseEntity<String>> listarFuncionariosElegiveis(String authorization, String correlationId) {
                return Mono.error(new UnsupportedOperationException());
            }
        };

        WebTestClient client = WebTestClient.bindToController(new ProfessorReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/turmas/{turmaId}/professores", turmaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-4")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].turmaId").isEqualTo(turmaId.toString())
                .jsonPath("$[0].professorNome").isEqualTo("Ana Souza");
    }

    @Test
    void deveExporContratoCompativelNaListagemDeFuncionariosElegiveis() {
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
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> listarAlocacoesPorProfessor(
                    String authorization,
                    String correlationId,
                    java.util.UUID professorId) {
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> listarProfessoresPorTurma(
                    String authorization,
                    String correlationId,
                    java.util.UUID turmaId) {
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> listarFuncionariosElegiveis(String authorization, String correlationId) {
                return Mono.just(ResponseEntity.ok("""
                        [{
                          "funcionarioId":"00000000-0000-0000-0000-000000000031",
                          "pessoaId":"00000000-0000-0000-0000-000000000021",
                          "nomeCompleto":"Ana Souza",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao",
                          "cargo":"Professor",
                          "ativo":true,
                          "elegivelProfessor":true
                        }]
                        """));
            }
        };

        WebTestClient client = WebTestClient.bindToController(new ProfessorReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/professores/funcionarios-elegiveis")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-5")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].funcionarioId").isEqualTo("00000000-0000-0000-0000-000000000031")
                .jsonPath("$[0].elegivelProfessor").isEqualTo(true);
    }
}
