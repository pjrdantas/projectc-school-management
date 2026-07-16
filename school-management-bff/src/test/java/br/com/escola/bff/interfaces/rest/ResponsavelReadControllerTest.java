package br.com.escola.bff.interfaces.rest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarResponsavelUseCase;
import reactor.core.publisher.Mono;

class ResponsavelReadControllerTest {

    @Test
    void deveDelegarLeituraOficialDeResponsaveisComFiltrosMinimos() {
        ConsultarResponsavelUseCase useCase = org.mockito.Mockito.mock(ConsultarResponsavelUseCase.class);
        when(useCase.listarResponsaveis("Bearer token", "corr-responsavel-0", "Maria", "98765432100"))
                .thenReturn(Mono.just(ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                [{"id":"00000000-0000-0000-0000-000000000601","nomeCompleto":"Maria Souza"}]
                                """)));

        WebTestClient client = WebTestClient.bindToController(new ResponsavelReadController(useCase))
                .build();

        client.get().uri(uriBuilder -> uriBuilder.path("/api/responsaveis")
                        .queryParam("nome", "Maria")
                        .queryParam("cpf", "98765432100")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-responsavel-0")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].nomeCompleto").isEqualTo("Maria Souza");

        verify(useCase).listarResponsaveis("Bearer token", "corr-responsavel-0", "Maria", "98765432100");
    }

    @Test
    void deveDelegarLeituraOficialDeResponsavelPorId() {
        ConsultarResponsavelUseCase useCase = org.mockito.Mockito.mock(ConsultarResponsavelUseCase.class);
        UUID responsavelId = UUID.fromString("00000000-0000-0000-0000-000000000601");
        when(useCase.buscarResponsavelPorId("Bearer token", "corr-responsavel-1", responsavelId))
                .thenReturn(Mono.just(ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"id":"00000000-0000-0000-0000-000000000601","nomeCompleto":"Responsavel Teste"}
                                """)));

        WebTestClient client = WebTestClient.bindToController(new ResponsavelReadController(useCase))
                .build();

        client.get().uri("/api/responsaveis/{id}", responsavelId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-responsavel-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.nomeCompleto").isEqualTo("Responsavel Teste");

        verify(useCase).buscarResponsavelPorId("Bearer token", "corr-responsavel-1", responsavelId);
    }
}
