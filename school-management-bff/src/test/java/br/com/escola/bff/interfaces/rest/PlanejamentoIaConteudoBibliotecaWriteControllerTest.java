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
import br.com.escola.bff.application.usecase.PublicarPlanejamentoIaConteudoBibliotecaUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class PlanejamentoIaConteudoBibliotecaWriteControllerTest {

    @Test
    void deveExporContratoCompativelNaPublicacaoOficialDeConteudoIaNaBiblioteca() {
        UUID conteudoId = UUID.fromString("00000000-0000-0000-0000-000000001011");
        CriarPlanejamentoIaConteudoUseCase criarConteudoUseCase =
                (authorization, correlationId, planejamentoId, requestBody) -> Mono.error(new UnsupportedOperationException());
        CriarPlanejamentoIaConteudoVersaoUseCase criarVersaoUseCase =
                (authorization, correlationId, requestedConteudoId, requestBody) -> Mono.error(new UnsupportedOperationException());
        AprovarPlanejamentoIaConteudoVersaoUseCase aprovarVersaoUseCase =
                (authorization, correlationId, requestedConteudoId, requestBody) -> Mono.error(new UnsupportedOperationException());
        PublicarPlanejamentoIaConteudoBibliotecaUseCase publicarBibliotecaUseCase =
                (authorization, correlationId, requestedConteudoId) -> Mono.just(ResponseEntity.status(201).body("""
                        {
                          "id":"00000000-0000-0000-0000-000000002111",
                          "origem":"PLANEJAMENTO_IA",
                          "professorNome":"Professor Um"
                        }
                        """));

        WebTestClient client = WebTestClient.bindToController(
                        new PlanejamentoIaConteudoWriteController(
                                criarConteudoUseCase,
                                criarVersaoUseCase,
                                aprovarVersaoUseCase,
                                publicarBibliotecaUseCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.post().uri("/api/ia/conteudos/{conteudoId}/publicar-biblioteca", conteudoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-planejamento-biblioteca-write-1")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.origem").isEqualTo("PLANEJAMENTO_IA")
                .jsonPath("$.professorNome").isEqualTo("Professor Um");
    }
}
