package br.com.escola.bff.infra.webclient;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.DisciplinaQuery;
import br.com.escola.bff.application.exception.DownstreamRejectedException;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.infra.config.LegacyClientProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.test.StepVerifier;

class LegacyDisciplinaClientTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void devePreservarContratoEPropagarSomenteHeadersConfiaveis() throws Exception {
        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000001",
                          "nome":"Matematica",
                          "cargaHoraria":80,
                          "status":"ATIVA",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao",
                          "createdAt":"2026-06-19T10:00:00"
                        }]
                        """));
        LegacyDisciplinaClient client = client(Duration.ofSeconds(2));

        StepVerifier.create(client.listar(new DisciplinaQuery("Bearer opaque-token", "corr-123")))
                .assertNext(result -> {
                    assertThat(result).hasSize(1);
                    assertThat(result.getFirst().nome()).isEqualTo("Matematica");
                    assertThat(result.getFirst().escolaNome()).isEqualTo("Escola padrao");
                })
                .verifyComplete();

        var request = server.takeRequest();
        assertThat(request.getPath()).isEqualTo("/api/disciplinas");
        assertThat(request.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(request.getHeader(TrustedHeaders.CORRELATION_ID)).isEqualTo("corr-123");
        assertThat(request.getHeader("X-Escola-Id")).isNull();
        assertThat(request.getHeader("X-Usuario-Id")).isNull();
    }

    @Test
    void devePreservarRejeicaoDeAutenticacaoDoMonolito() {
        server.enqueue(new MockResponse().setResponseCode(401));
        LegacyDisciplinaClient client = client(Duration.ofSeconds(2));

        StepVerifier.create(client.listar(new DisciplinaQuery("Bearer invalid", "corr-1")))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(DownstreamRejectedException.class);
                    assertThat(((DownstreamRejectedException) error).status()).isEqualTo(401);
                })
                .verify();
    }

    @Test
    void deveAplicarTimeoutControlado() {
        server.enqueue(new MockResponse()
                .setBody("[]")
                .setBodyDelay(500, java.util.concurrent.TimeUnit.MILLISECONDS));
        LegacyDisciplinaClient client = client(Duration.ofMillis(50));

        StepVerifier.create(client.listar(new DisciplinaQuery("Bearer token", "corr-1")))
                .expectError(DownstreamUnavailableException.class)
                .verify();
    }

    private LegacyDisciplinaClient client(Duration responseTimeout) {
        URI baseUrl = server.url("/").uri();
        LegacyClientProperties properties = new LegacyClientProperties(
                baseUrl, Duration.ofSeconds(1), responseTimeout);
        return new LegacyDisciplinaClient(
                WebClient.builder().baseUrl(baseUrl.toString()).build(),
                CircuitBreaker.ofDefaults("test-monolith"),
                properties);
    }
}


