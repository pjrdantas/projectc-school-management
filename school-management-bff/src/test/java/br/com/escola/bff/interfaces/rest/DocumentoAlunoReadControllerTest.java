package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarDocumentoAlunoUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class DocumentoAlunoReadControllerTest {

    @Test
    void deveExporContratoCompativelNaBuscaDeDocumentoPorId() {
        UUID documentoId = UUID.fromString("00000000-0000-0000-0000-000000000301");
        ConsultarDocumentoAlunoUseCase useCase = new ConsultarDocumentoAlunoUseCase() {
            @Override
            public Mono<ResponseEntity<String>> listarDocumentosPorAluno(
                    String authorization,
                    String correlationId,
                    UUID alunoId) {
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> buscarDocumentoAlunoPorId(
                    String authorization,
                    String correlationId,
                    UUID id) {
                return Mono.just(ResponseEntity.ok("""
                        {
                          "id":"00000000-0000-0000-0000-000000000301",
                          "alunoId":"00000000-0000-0000-0000-000000000021",
                          "tipoDocumento":"HISTORICO_ESCOLAR",
                          "nomeArquivo":"historico.pdf",
                          "urlArquivo":"s3://bucket/historico.pdf",
                          "numeroDocumento":"historico.pdf",
                          "caminhoArquivo":"s3://bucket/historico.pdf",
                          "dataUpload":"2026-07-12T10:00:00",
                          "observacao":"Documento escolar"
                        }
                        """));
            }
        };

        WebTestClient client = WebTestClient.bindToController(new DocumentoAlunoReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/documentos-alunos/{id}", documentoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-doc-aluno-id-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(documentoId.toString())
                .jsonPath("$.tipoDocumento").isEqualTo("HISTORICO_ESCOLAR");
    }

    @Test
    void deveExporContratoCompativelNaListagemDeDocumentosPorAluno() {
        UUID alunoId = UUID.fromString("00000000-0000-0000-0000-000000000021");
        ConsultarDocumentoAlunoUseCase useCase = new ConsultarDocumentoAlunoUseCase() {
            @Override
            public Mono<ResponseEntity<String>> listarDocumentosPorAluno(
                    String authorization,
                    String correlationId,
                    UUID requestedAlunoId) {
                return Mono.just(ResponseEntity.ok("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000301",
                          "alunoId":"00000000-0000-0000-0000-000000000021",
                          "tipoDocumento":"HISTORICO_ESCOLAR",
                          "nomeArquivo":"historico.pdf",
                          "urlArquivo":"s3://bucket/historico.pdf",
                          "numeroDocumento":"historico.pdf",
                          "caminhoArquivo":"s3://bucket/historico.pdf",
                          "dataUpload":"2026-07-12T10:00:00",
                          "observacao":"Documento escolar"
                        }]
                        """));
            }

            @Override
            public Mono<ResponseEntity<String>> buscarDocumentoAlunoPorId(
                    String authorization,
                    String correlationId,
                    UUID id) {
                return Mono.error(new UnsupportedOperationException());
            }
        };

        WebTestClient client = WebTestClient.bindToController(new DocumentoAlunoReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/documentos-alunos/alunos/{alunoId}", alunoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-doc-aluno-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].alunoId").isEqualTo(alunoId.toString())
                .jsonPath("$[0].tipoDocumento").isEqualTo("HISTORICO_ESCOLAR");
    }
}
