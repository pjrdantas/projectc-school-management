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
import br.com.escola.bff.application.usecase.ConsultarAlunoResponsavelUseCase;
import reactor.core.publisher.Mono;

class AlunoResponsavelReadControllerTest {

    @Test
    void deveDelegarLeituraOficialDeResponsaveisPorAluno() {
        ConsultarAlunoResponsavelUseCase useCase = org.mockito.Mockito.mock(ConsultarAlunoResponsavelUseCase.class);
        UUID alunoId = UUID.fromString("00000000-0000-0000-0000-000000000301");
        when(useCase.listarResponsaveisPorAluno("Bearer token", "corr-resp-1", alunoId))
                .thenReturn(Mono.just(ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                [{"id":"00000000-0000-0000-0000-000000000401","nomeCompleto":"Responsavel Teste"}]
                                """)));

        WebTestClient client = WebTestClient.bindToController(new AlunoResponsavelReadController(useCase))
                .build();

        client.get().uri("/api/alunos/{alunoId}/responsaveis", alunoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-resp-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].nomeCompleto").isEqualTo("Responsavel Teste");

        verify(useCase).listarResponsaveisPorAluno("Bearer token", "corr-resp-1", alunoId);
    }
}
