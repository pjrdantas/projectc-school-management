package br.com.escola.peopleservice.infra.webclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.infra.config.ResponsaveisAlunoClientProperties;

class ResponsaveisAlunoHttpAdapterTest {

    @Test
    void deveConsultarResponsaveisComContextoInterno() {
        UUID alunoId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        RestClient.Builder builder = RestClient.builder().baseUrl("http://responsibles.local");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ResponsaveisAlunoHttpAdapter adapter = new ResponsaveisAlunoHttpAdapter(
                builder.build(),
                new ResponsaveisAlunoClientProperties(
                        URI.create("http://responsibles.local"), "responsibles-token",
                        Duration.ofSeconds(1), Duration.ofSeconds(1)));

        server.expect(requestTo("http://responsibles.local/internal/v1/alunos/" + alunoId + "/responsaveis"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(header("X-Internal-Token", "responsibles-token"))
                .andExpect(header("X-Correlation-Id", "corr-ficha"))
                .andExpect(header("X-Usuario-Id", usuarioId.toString()))
                .andExpect(header("X-Escola-Id", escolaId.toString()))
                .andRespond(withSuccess("""
                        [{"id":"%s","nomeCompleto":"Responsavel","parentesco":"MAE"}]
                        """.formatted(UUID.randomUUID()), MediaType.APPLICATION_JSON));

        var response = adapter.listarResponsaveisPorAluno(
                alunoId,
                "Bearer access-token",
                new InternalRequestContext("corr-ficha", usuarioId, escolaId));

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().parentesco()).isEqualTo("MAE");
        server.verify();
    }

    @Test
    void deveTratarNaoEncontradoComoListaVazia() {
        UUID alunoId = UUID.randomUUID();
        RestClient.Builder builder = RestClient.builder().baseUrl("http://responsibles.local");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ResponsaveisAlunoHttpAdapter adapter = new ResponsaveisAlunoHttpAdapter(
                builder.build(),
                new ResponsaveisAlunoClientProperties(
                        URI.create("http://responsibles.local"), "", Duration.ofSeconds(1), Duration.ofSeconds(1)));
        server.expect(requestTo("http://responsibles.local/internal/v1/alunos/" + alunoId + "/responsaveis"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        var response = adapter.listarResponsaveisPorAluno(
                alunoId,
                "Bearer access-token",
                new InternalRequestContext("corr-ficha", UUID.randomUUID(), UUID.randomUUID()));

        assertThat(response).isEmpty();
        server.verify();
    }
}
