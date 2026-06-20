package br.com.escola.bff.interfaces.rest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.DisciplinaView;
import br.com.escola.bff.application.usecase.ListarDisciplinasUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class DisciplinaControllerTest {

    @Test
    void deveExporContratoCompativelComMonolito() {
        DisciplinaView disciplina = new DisciplinaView(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Matematica", 80, "ATIVA",
                UUID.fromString("00000000-0000-0000-0000-000000000047"),
                "Escola padrao", LocalDateTime.of(2026, 6, 19, 10, 0));
        ListarDisciplinasUseCase useCase = query -> Mono.just(List.of(disciplina));
        WebTestClient client = WebTestClient.bindToController(new DisciplinaController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/disciplinas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].nome").isEqualTo("Matematica")
                .jsonPath("$[0].escolaId").isEqualTo("00000000-0000-0000-0000-000000000047");
    }
}
