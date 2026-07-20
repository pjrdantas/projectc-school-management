package br.com.escola.peopleservice.infra.webclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.model.EscolaPessoa;
import br.com.escola.peopleservice.infra.config.EscolaPessoaClientProperties;

class EscolaPessoaHttpAdapterTest {

    @Test
    void deveConsultarEscolaComContextoInterno() {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        RestClient.Builder builder = RestClient.builder().baseUrl("http://tenant.local");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        EscolaPessoaHttpAdapter adapter = new EscolaPessoaHttpAdapter(
                builder.build(),
                new EscolaPessoaClientProperties(
                        URI.create("http://tenant.local"),
                        "tenant-token",
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(1)));

        server.expect(requestTo("http://tenant.local/internal/v1/escolas/" + escolaId))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(header("X-Internal-Token", "tenant-token"))
                .andExpect(header("X-Correlation-Id", "corr-escola-pessoa"))
                .andExpect(header("X-Usuario-Id", usuarioId.toString()))
                .andExpect(header("X-Escola-Id", escolaId.toString()))
                .andRespond(withSuccess("""
                        {"id":"%s","nome":"Escola B3","ativo":true}
                        """.formatted(escolaId), MediaType.APPLICATION_JSON));

        EscolaPessoa escola = adapter.buscar(
                escolaId,
                "Bearer access-token",
                new InternalRequestContext("corr-escola-pessoa", usuarioId, escolaId));

        assertThat(escola).isEqualTo(new EscolaPessoa(escolaId, "Escola B3", true));
        server.verify();
    }
}
