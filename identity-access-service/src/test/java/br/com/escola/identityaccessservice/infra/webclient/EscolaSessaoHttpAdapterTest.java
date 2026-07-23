package br.com.escola.identityaccessservice.infra.webclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import br.com.escola.identityaccessservice.application.context.InternalRequestContext;
import br.com.escola.identityaccessservice.application.model.EscolaDisponivel;
import br.com.escola.identityaccessservice.infra.config.EscolaSessaoClientProperties;

class EscolaSessaoHttpAdapterTest {

    @Test
    void deveConsultarEscolasComContextoConfiavelDaSessao() {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        RestClient.Builder builder = RestClient.builder().baseUrl("http://tenant.local");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        EscolaSessaoHttpAdapter adapter = new EscolaSessaoHttpAdapter(
                builder.build(),
                new EscolaSessaoClientProperties(
                        URI.create("http://tenant.local"),
                        "tenant-token",
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(1)));

        server.expect(requestTo("http://tenant.local/internal/v1/tenant/escolas"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(header("X-Internal-Token", "tenant-token"))
                .andExpect(header("X-Correlation-Id", "corr-escolas"))
                .andExpect(header("X-Usuario-Id", usuarioId.toString()))
                .andExpect(header("X-Escola-Id", escolaId.toString()))
                .andRespond(withSuccess("""
                        [{
                          "escolaId":"%s",
                          "escolaNome":"Escola Teste",
                          "ativa":true
                        }]
                        """.formatted(escolaId), MediaType.APPLICATION_JSON));

        List<EscolaDisponivel> escolas = adapter.listarDisponiveis(
                "Bearer access-token",
                new InternalRequestContext("corr-escolas", usuarioId, escolaId));

        assertThat(escolas).containsExactly(new EscolaDisponivel(escolaId, "Escola Teste", true));
        server.verify();
    }
}
