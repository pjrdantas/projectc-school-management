package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarMatriculaUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class MatriculaReadControllerTest {

    @Test
    void deveExporContratoCompativelNaListagemDeMatriculas() {
        UUID alunoId = UUID.fromString("00000000-0000-0000-0000-000000000021");
        ConsultarMatriculaUseCase useCase = new ConsultarMatriculaUseCase() {
            @Override
            public Mono<ResponseEntity<String>> listarStatus(String authorization, String correlationId) {
                return Mono.just(ResponseEntity.ok("[]"));
            }

            @Override
            public Mono<ResponseEntity<String>> listarMatriculas(
                    String authorization,
                    String correlationId,
                    UUID requestedAlunoId,
                    UUID turmaId,
                    UUID periodoLetivoId,
                    String status) {
                return Mono.just(ResponseEntity.ok("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000701",
                          "alunoId":"00000000-0000-0000-0000-000000000021",
                          "turmaId":"00000000-0000-0000-0000-000000000071",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao",
                          "serieId":"00000000-0000-0000-0000-000000000081",
                          "serieNome":"6 Ano",
                          "periodoLetivoId":"00000000-0000-0000-0000-000000000091",
                          "status":"EM_ANDAMENTO",
                          "tipoMatricula":"PRIMEIRA_MATRICULA",
                          "dataMatricula":"2026-07-12",
                          "observacao":"Matricula BFF",
                          "createdAt":"2026-07-12T10:00:00",
                          "etapas":[{
                            "id":"00000000-0000-0000-0000-000000000801",
                            "descricao":"Analise documental",
                            "ordem":1,
                            "status":"PENDENTE",
                            "dataInicio":"2026-07-12T10:00:00",
                            "dataConclusao":null,
                            "observacao":"Aguardando conferencia"
                          }]
                        }]
                        """));
            }
        };

        WebTestClient client = WebTestClient.bindToController(new MatriculaReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri(uriBuilder -> uriBuilder.path("/api/matriculas")
                        .queryParam("alunoId", alunoId)
                        .queryParam("status", "EM_ANDAMENTO")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-matricula-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].alunoId").isEqualTo(alunoId.toString())
                .jsonPath("$[0].status").isEqualTo("EM_ANDAMENTO")
                .jsonPath("$[0].etapas[0].descricao").isEqualTo("Analise documental");
    }
}
