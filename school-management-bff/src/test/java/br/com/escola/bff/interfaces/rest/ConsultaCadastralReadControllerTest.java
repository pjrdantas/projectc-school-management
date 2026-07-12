package br.com.escola.bff.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarCadastroPessoaUseCase;
import br.com.escola.bff.interfaces.advice.BffExceptionHandler;
import reactor.core.publisher.Mono;

class ConsultaCadastralReadControllerTest {

    @Test
    void deveExporContratoCompativelDaConsultaCadastral() {
        ConsultarCadastroPessoaUseCase useCase = (
                authorization,
                correlationId,
                nomeAluno,
                cpfAluno,
                nomeResponsavel,
                cpfResponsavel,
                page,
                size) -> Mono.just(ResponseEntity.ok("""
                        {
                          "content":[{
                            "alunoId":"00000000-0000-0000-0000-000000000301",
                            "nomeAluno":"Aluno Teste",
                            "responsaveis":[]
                          }],
                          "totalElements":1,
                          "page":0,
                          "size":20
                        }
                        """));

        WebTestClient client = WebTestClient.bindToController(new ConsultaCadastralReadController(useCase))
                .controllerAdvice(new BffExceptionHandler())
                .build();

        client.get().uri("/api/consulta-cadastral?nomeAluno=Aluno&page=0&size=20")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-consulta-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].nomeAluno").isEqualTo("Aluno Teste")
                .jsonPath("$.totalElements").isEqualTo(1);
    }
}
