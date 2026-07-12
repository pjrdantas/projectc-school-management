package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarTransferenciaUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class TransferenciaReadControllerTest {

    @Test
    void deveExporContratoCompativelNaListagemDeTransferenciasPorAluno() {
        UUID alunoId = UUID.fromString("00000000-0000-0000-0000-000000000021");
        ConsultarTransferenciaUseCase useCase = new ConsultarTransferenciaUseCase() {
            @Override
            public Mono<ResponseEntity<String>> listarTransferenciasPorAluno(
                    String authorization,
                    String correlationId,
                    UUID requestedAlunoId) {
                return Mono.just(ResponseEntity.ok("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000091",
                          "alunoId":"00000000-0000-0000-0000-000000000021",
                          "serieOrigem":"5A",
                          "anoLetivoOrigem":"2026",
                          "statusTransferencia":"EM_ANDAMENTO"
                        }]
                        """));
            }

            @Override
            public Mono<ResponseEntity<String>> buscarTransferenciaPorId(
                    String authorization,
                    String correlationId,
                    UUID transferenciaId) {
                return Mono.error(new UnsupportedOperationException());
            }
        };

        WebTestClient client = WebTestClient.bindToController(new TransferenciaReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/transferencias/alunos/{alunoId}", alunoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-transferencia-list-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].alunoId").isEqualTo(alunoId.toString())
                .jsonPath("$[0].statusTransferencia").isEqualTo("EM_ANDAMENTO");
    }

    @Test
    void deveExporContratoCompativelNaBuscaDeTransferenciaPorId() {
        UUID transferenciaId = UUID.fromString("00000000-0000-0000-0000-000000000091");
        ConsultarTransferenciaUseCase useCase = new ConsultarTransferenciaUseCase() {
            @Override
            public Mono<ResponseEntity<String>> listarTransferenciasPorAluno(
                    String authorization,
                    String correlationId,
                    UUID alunoId) {
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> buscarTransferenciaPorId(
                    String authorization,
                    String correlationId,
                    UUID requestedTransferenciaId) {
                return Mono.just(ResponseEntity.ok("""
                        {
                          "id":"00000000-0000-0000-0000-000000000091",
                          "alunoId":"00000000-0000-0000-0000-000000000021",
                          "serieOrigem":"5A",
                          "anoLetivoOrigem":"2026",
                          "statusTransferencia":"EM_ANDAMENTO"
                        }
                        """));
            }
        };

        WebTestClient client = WebTestClient.bindToController(new TransferenciaReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/transferencias/{transferenciaId}", transferenciaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-transferencia-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(transferenciaId.toString())
                .jsonPath("$.statusTransferencia").isEqualTo("EM_ANDAMENTO");
    }
}
