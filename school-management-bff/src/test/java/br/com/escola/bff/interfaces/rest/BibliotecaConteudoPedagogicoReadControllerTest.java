package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarBibliotecaConteudoPedagogicoUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class BibliotecaConteudoPedagogicoReadControllerTest {

    @Test
    void deveExporContratoCompativelNaListagemOficialDaBibliotecaPedagogica() {
        UUID professorId = UUID.fromString("00000000-0000-0000-0000-000000000211");
        UUID disciplinaId = UUID.fromString("00000000-0000-0000-0000-000000000311");
        ConsultarBibliotecaConteudoPedagogicoUseCase useCase =
                (authorization, correlationId, requestedProfessorId, requestedDisciplinaId, tipoConteudo, tema) ->
                        Mono.just(ResponseEntity.ok("""
                                [{
                                  "id":"00000000-0000-0000-0000-000000000411",
                                  "escolaId":"00000000-0000-0000-0000-000000000047",
                                  "escolaNome":"Escola padrao",
                                  "professorId":"00000000-0000-0000-0000-000000000211",
                                  "professorNome":"Professor Um",
                                  "disciplinaId":"00000000-0000-0000-0000-000000000311",
                                  "disciplinaNome":"Matematica",
                                  "tipoConteudo":"ATIVIDADE",
                                  "tipoConteudoDescricao":"Atividade",
                                  "titulo":"Lista",
                                  "tema":"Fracoes",
                                  "conteudo":"Conteudo gerado",
                                  "origem":"PLANEJAMENTO_IA",
                                  "reutilizavel":true,
                                  "ativo":true,
                                  "createdAt":"2026-07-13T10:15:30",
                                  "updatedAt":"2026-07-13T10:15:30"
                                }]
                                """));

        WebTestClient client = WebTestClient.bindToController(
                        new BibliotecaConteudoPedagogicoReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri(uriBuilder -> uriBuilder.path("/api/biblioteca-conteudos-pedagogicos")
                        .queryParam("professorId", professorId)
                        .queryParam("disciplinaId", disciplinaId)
                        .queryParam("tipoConteudo", "ATIVIDADE")
                        .queryParam("tema", "Fracoes")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-biblioteca-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].professorId").isEqualTo(professorId.toString())
                .jsonPath("$[0].disciplinaId").isEqualTo(disciplinaId.toString())
                .jsonPath("$[0].tipoConteudo").isEqualTo("ATIVIDADE");
    }
}
