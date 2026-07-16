package br.com.escola.responsiblesservice.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import br.com.escola.responsiblesservice.infra.config.ResponsiblesMonolithClientProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest
@AutoConfigureMockMvc
@EnableConfigurationProperties(ResponsiblesMonolithClientProperties.class)
@ImportAutoConfiguration({
        RestClientAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class
})
class ResponsavelInternalControllerIntegrationTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void beforeAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void afterAll() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("responsibles.internal-api.token", () -> "responsibles-token");
        registry.add("responsibles.monolith.base-url", () -> mockWebServer.url("/").toString());
    }

    @Test
    void deveConsultarResponsavelPorIdNoContratoInterno() throws Exception {
        UUID responsavelId = UUID.fromString("00000000-0000-0000-0000-000000000601");
        UUID usuarioId = UUID.fromString("00000000-0000-0000-0000-000000000101");
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id":"%s",
                          "nomeCompleto":"Responsavel Interno"
                        }
                        """.formatted(responsavelId)));

        mockMvc.perform(get("/internal/v1/responsaveis/{id}", responsavelId)
                        .header("Authorization", "Bearer opaque-token")
                        .header("X-Internal-Token", "responsibles-token")
                        .header("X-Correlation-Id", "corr-responsavel-1")
                        .header("X-Usuario-Id", usuarioId)
                        .header("X-Escola-Id", escolaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responsavelId.toString()))
                .andExpect(jsonPath("$.nomeCompleto").value("Responsavel Interno"));

        var request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/api/responsaveis/" + responsavelId);
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer opaque-token");
        assertThat(request.getHeader("X-Correlation-Id")).isEqualTo("corr-responsavel-1");
        assertThat(request.getHeader("X-Usuario-Id")).isEqualTo(usuarioId.toString());
        assertThat(request.getHeader("X-Escola-Id")).isEqualTo(escolaId.toString());
    }
}
