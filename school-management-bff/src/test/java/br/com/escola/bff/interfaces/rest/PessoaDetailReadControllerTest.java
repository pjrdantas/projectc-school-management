package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarPessoaDetalheUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class PessoaDetailReadControllerTest {

    @Test
    void deveExporContratoCompativelDePessoaPorId() {
        UUID pessoaId = UUID.fromString("00000000-0000-0000-0000-000000000401");
        ConsultarPessoaDetalheUseCase useCase = new StubUseCase() {
            @Override
            public Mono<ResponseEntity<String>> buscarPessoaPorId(String authorization, String correlationId, UUID requestedId) {
                return Mono.just(ResponseEntity.ok("""
                        {
                          "id":"%s",
                          "nomeCompleto":"Pessoa Interna",
                          "cpf":"12345678900"
                        }
                        """.formatted(requestedId)));
            }
        };

        WebTestClient client = WebTestClient.bindToController(new PessoaDetailReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/pessoas/{pessoaId}", pessoaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pessoa-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(pessoaId.toString())
                .jsonPath("$.nomeCompleto").isEqualTo("Pessoa Interna");
    }

    @Test
    void deveExporContratoCompativelDeDocumentosPorPessoa() {
        UUID pessoaId = UUID.fromString("00000000-0000-0000-0000-000000000402");
        ConsultarPessoaDetalheUseCase useCase = new StubUseCase() {
            @Override
            public Mono<ResponseEntity<String>> listarDocumentos(String authorization, String correlationId, UUID requestedId) {
                return Mono.just(ResponseEntity.ok("""
                        [{
                          "documentoId":"00000000-0000-0000-0000-000000000501",
                          "numeroDocumento":"ABC123"
                        }]
                        """));
            }
        };

        WebTestClient client = WebTestClient.bindToController(new PessoaDetailReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/pessoas/{pessoaId}/documentos", pessoaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pessoa-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].numeroDocumento").isEqualTo("ABC123");
    }

    private abstract static class StubUseCase implements ConsultarPessoaDetalheUseCase {

        @Override
        public Mono<ResponseEntity<String>> buscarPessoaPorId(String authorization, String correlationId, UUID pessoaId) {
            return Mono.error(new UnsupportedOperationException());
        }

        @Override
        public Mono<ResponseEntity<String>> buscarEnderecoPrincipal(String authorization, String correlationId, UUID pessoaId) {
            return Mono.error(new UnsupportedOperationException());
        }

        @Override
        public Mono<ResponseEntity<String>> listarEnderecos(String authorization, String correlationId, UUID pessoaId) {
            return Mono.error(new UnsupportedOperationException());
        }

        @Override
        public Mono<ResponseEntity<String>> buscarContato(String authorization, String correlationId, UUID pessoaId) {
            return Mono.error(new UnsupportedOperationException());
        }

        @Override
        public Mono<ResponseEntity<String>> listarDocumentos(String authorization, String correlationId, UUID pessoaId) {
            return Mono.error(new UnsupportedOperationException());
        }

        @Override
        public Mono<ResponseEntity<String>> buscarDocumentoPorId(String authorization, String correlationId, UUID documentoId) {
            return Mono.error(new UnsupportedOperationException());
        }
    }
}
