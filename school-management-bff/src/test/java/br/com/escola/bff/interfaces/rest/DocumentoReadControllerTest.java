package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarDocumentoUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class DocumentoReadControllerTest {

    @Test
    void deveExporContratoCompativelNaListagemDeDocumentosPorEntidade() {
        UUID entidadeId = UUID.fromString("00000000-0000-0000-0000-000000000701");
        ConsultarDocumentoUseCase useCase = (authorization, correlationId, entidadeTipo, requestedEntidadeId) ->
                Mono.just(ResponseEntity.ok("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000901",
                          "entidadeTipo":"MATRICULA",
                          "entidadeId":"00000000-0000-0000-0000-000000000701",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao",
                          "tipoDocumento":"CPF",
                          "numeroDocumento":"12345678900",
                          "caminhoArquivo":"s3://bucket/documento.pdf",
                          "dataUpload":"2026-07-12T10:00:00",
                          "observacao":"Documento administrativo"
                        }]
                        """));

        WebTestClient client = WebTestClient.bindToController(new DocumentoReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri(uriBuilder -> uriBuilder.path("/api/documentos")
                        .queryParam("entidadeTipo", "MATRICULA")
                        .queryParam("entidadeId", entidadeId)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-documento-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].entidadeTipo").isEqualTo("MATRICULA")
                .jsonPath("$[0].entidadeId").isEqualTo(entidadeId.toString())
                .jsonPath("$[0].tipoDocumento").isEqualTo("CPF");
    }
}
