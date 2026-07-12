package br.com.escola.bff.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarPessoaCatalogoUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class PessoaCatalogReadControllerTest {

    @Test
    void deveExporContratoCompativelDeTiposPessoa() {
        ConsultarPessoaCatalogoUseCase useCase = new ConsultarPessoaCatalogoUseCase() {
            @Override
            public Mono<ResponseEntity<String>> listarTiposPessoa(String authorization, String correlationId) {
                return Mono.just(ResponseEntity.ok("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000201",
                          "codigo":"ALUNO",
                          "descricao":"Aluno"
                        }]
                        """));
            }

            @Override
            public Mono<ResponseEntity<String>> listarTiposEndereco(String authorization, String correlationId) {
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> listarStatusAluno(String authorization, String correlationId) {
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> listarParentescos(String authorization, String correlationId) {
                return Mono.error(new UnsupportedOperationException());
            }
        };

        WebTestClient client = WebTestClient.bindToController(new PessoaCatalogReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/pessoas/catalogos/tipos-pessoa")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-cat-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].codigo").isEqualTo("ALUNO");
    }

    @Test
    void deveExporContratoCompativelDeParentescos() {
        ConsultarPessoaCatalogoUseCase useCase = new ConsultarPessoaCatalogoUseCase() {
            @Override
            public Mono<ResponseEntity<String>> listarTiposPessoa(String authorization, String correlationId) {
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> listarTiposEndereco(String authorization, String correlationId) {
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> listarStatusAluno(String authorization, String correlationId) {
                return Mono.error(new UnsupportedOperationException());
            }

            @Override
            public Mono<ResponseEntity<String>> listarParentescos(String authorization, String correlationId) {
                return Mono.just(ResponseEntity.ok("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000301",
                          "codigo":"MAE",
                          "descricao":"Mae"
                        }]
                        """));
            }
        };

        WebTestClient client = WebTestClient.bindToController(new PessoaCatalogReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/pessoas/catalogos/parentescos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-cat-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].codigo").isEqualTo("MAE");
    }
}
