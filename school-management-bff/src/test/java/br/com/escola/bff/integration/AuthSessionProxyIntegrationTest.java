package br.com.escola.bff.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class AuthSessionProxyIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer IDENTITY_ACCESS = startServer();
    private static final MockWebServer INSTITUTIONAL_TENANT = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.identity-access-service.base-url", () -> IDENTITY_ACCESS.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("clients.institutional-tenant-service.base-url", () -> INSTITUTIONAL_TENANT.url("/").toString());
        registry.add("clients.institutional-tenant-service.internal-token", () -> "institutional-tenant-internal-token");
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        MONOLITH.shutdown();
        IDENTITY_ACCESS.shutdown();
        INSTITUTIONAL_TENANT.shutdown();
    }

    @Test
    void deveConsumirIdentityAccessServiceNaListagemOficialDeEscolasDaSessao() throws InterruptedException {
        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        INSTITUTIONAL_TENANT.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setHeader(TrustedHeaders.CORRELATION_ID, "corr-auth-1")
                .setBody("""
                        [
                          {
                            "escolaId":"00000000-0000-0000-0000-000000000047",
                            "escolaNome":"Escola padrao",
                            "ativa":true
                          }
                        ]
                        """));

        client.get().uri("/api/auth/escolas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-auth-1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(TrustedHeaders.CORRELATION_ID, "corr-auth-1")
                .expectBody()
                .jsonPath("$[0].ativa").isEqualTo(true);

        var contextRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        assertThat(contextRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(contextRequest.getHeader("X-Internal-Token")).isEqualTo("identity-access-internal-token");
        assertThat(contextRequest.getHeader("X-Correlation-Id")).isEqualTo("corr-auth-1");

        var institutionalRequest = INSTITUTIONAL_TENANT.takeRequest();
        assertThat(institutionalRequest.getPath()).isEqualTo("/internal/v1/tenant/escolas");
        assertThat(institutionalRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(institutionalRequest.getHeader("X-Internal-Token")).isEqualTo("institutional-tenant-internal-token");
        assertThat(institutionalRequest.getHeader("X-Correlation-Id")).isEqualTo("corr-auth-1");
        assertThat(institutionalRequest.getHeader("X-Usuario-Id")).isEqualTo("00000000-0000-0000-0000-000000000101");
        assertThat(institutionalRequest.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
        assertThat(MONOLITH.getRequestCount()).isZero();
    }

    @Test
    void deveConsumirIdentityAccessServiceNaTrocaOficialDeEscolaAtiva() throws InterruptedException {
        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000099",
                          "escolaNome":"Escola Secundaria",
                          "username":"usuario.teste"
                        }
                        """));

        client.post().uri("/api/auth/escola-ativa")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-auth-2")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "escolaId":"00000000-0000-0000-0000-000000000099"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.escolaId").isEqualTo("00000000-0000-0000-0000-000000000099")
                .jsonPath("$.escolaNome").isEqualTo("Escola Secundaria");

        var contextRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        var identityRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(identityRequest.getPath()).isEqualTo("/internal/v1/auth/escola-ativa");
        assertThat(identityRequest.getMethod()).isEqualTo("POST");
        assertThat(identityRequest.getBody().readUtf8()).contains("00000000-0000-0000-0000-000000000099");
        assertThat(identityRequest.getHeader("X-Usuario-Id")).isEqualTo("00000000-0000-0000-0000-000000000101");
        assertThat(identityRequest.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
        assertThat(MONOLITH.getRequestCount()).isZero();
    }

    @Test
    void deveExigirBearerTokenNasRotasOficiaisDeSessaoMultiescola() {
        client.get().uri("/api/auth/escolas")
                .header(TrustedHeaders.CORRELATION_ID, "corr-auth-3")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("UNAUTHORIZED");
    }

    @Test
    void deveOficializarCicloPublicoDeAutenticacaoSemMonolito() throws InterruptedException {
        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "accessToken":"access-1",
                          "refreshToken":"refresh-1",
                          "tokenType":"Bearer",
                          "nome":"Usuario Teste",
                          "perfis":["SECRETARIA"],
                          "permissoes":["MATRICULA_EDITAR"]
                        }
                        """));
        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "accessToken":"access-2",
                          "refreshToken":"refresh-2",
                          "tokenType":"Bearer",
                          "nome":"Usuario Teste",
                          "perfis":["SECRETARIA"],
                          "permissoes":["MATRICULA_EDITAR"]
                        }
                        """));
        IDENTITY_ACCESS.enqueue(new MockResponse().setResponseCode(204));

        client.post().uri("/api/auth/login")
                .header(TrustedHeaders.CORRELATION_ID, "corr-auth-login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"login\":\"usuario.teste\",\"senha\":\"senha\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("access-1")
                .jsonPath("$.perfis[0]").isEqualTo("SECRETARIA");

        var loginRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(loginRequest.getPath()).isEqualTo("/internal/v1/auth/login");
        assertThat(loginRequest.getHeader("X-Internal-Token")).isEqualTo("identity-access-internal-token");
        assertThat(loginRequest.getHeader("X-Correlation-Id")).isEqualTo("corr-auth-login");
        assertThat(loginRequest.getHeader(HttpHeaders.AUTHORIZATION)).isNull();
        assertThat(loginRequest.getBody().readUtf8()).contains("usuario.teste");

        client.post().uri("/api/auth/refresh")
                .header(TrustedHeaders.CORRELATION_ID, "corr-auth-refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"refreshToken\":\"refresh-1\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.refreshToken").isEqualTo("refresh-2");

        var refreshRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(refreshRequest.getPath()).isEqualTo("/internal/v1/auth/refresh");
        assertThat(refreshRequest.getBody().readUtf8()).contains("refresh-1");

        client.post().uri("/api/auth/logout")
                .header(TrustedHeaders.CORRELATION_ID, "corr-auth-logout")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"refreshToken\":\"refresh-2\"}")
                .exchange()
                .expectStatus().isNoContent();

        var logoutRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(logoutRequest.getPath()).isEqualTo("/internal/v1/auth/logout");
        assertThat(logoutRequest.getBody().readUtf8()).contains("refresh-2");
        assertThat(MONOLITH.getRequestCount()).isZero();
    }

    private static MockWebServer startServer() {
        MockWebServer server = new MockWebServer();
        try {
            server.start();
            return server;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
