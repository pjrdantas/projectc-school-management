package br.com.escola.identityaccessservice.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@SpringBootTest
@AutoConfigureMockMvc
class SessaoInternaControllerIntegrationTest {

    private static final UUID ESCOLA_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");

    private static MockWebServer mockWebServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
        registry.add("identity-access.internal-api.token", () -> "identity-token");
        registry.add("identity-access.monolith.base-url", () -> mockWebServer.url("/").toString());
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:identityaccess;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
    }

    @BeforeEach
    void prepararBanco() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS sessao_autenticacao");
        jdbcTemplate.execute("DROP TABLE IF EXISTS usuario");
        jdbcTemplate.execute("DROP TABLE IF EXISTS escola");

        jdbcTemplate.execute("""
                CREATE TABLE escola (
                    id_escola UUID PRIMARY KEY,
                    nome VARCHAR(150) NOT NULL,
                    ativo BOOLEAN NOT NULL,
                    created_at TIMESTAMP NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE usuario (
                    id_usuario UUID PRIMARY KEY,
                    username VARCHAR(80) NOT NULL,
                    nome VARCHAR(150) NOT NULL,
                    email VARCHAR(150) NOT NULL,
                    senha_hash VARCHAR(255) NOT NULL,
                    ativo BOOLEAN NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    id_escola UUID NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE sessao_autenticacao (
                    id_sessao_autenticacao UUID PRIMARY KEY,
                    id_usuario UUID NOT NULL,
                    id_escola UUID NULL,
                    refresh_token_hash VARCHAR(255) NOT NULL,
                    access_token_hash VARCHAR(255),
                    expira_em TIMESTAMP NOT NULL,
                    access_expira_em TIMESTAMP,
                    revogado BOOLEAN NOT NULL,
                    created_at TIMESTAMP NOT NULL
                )
                """);
    }

    @Test
    void deveListarEscolasDaSessaoNoContratoInterno() throws Exception {
        UUID escolaAtivaId = UUID.randomUUID();
        UUID outraEscolaId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "escolaId":"%s",
                            "escolaNome":"Escola Ativa",
                            "ativa":true
                          },
                          {
                            "escolaId":"%s",
                            "escolaNome":"Escola Opcional",
                            "ativa":false
                          }
                        ]
                        """.formatted(escolaAtivaId, outraEscolaId)));

        mockMvc.perform(get("/internal/v1/auth/escolas")
                        .header("X-Internal-Token", "identity-token")
                        .header("X-Correlation-Id", "corr-identity-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer identity-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].escolaId").value(escolaAtivaId.toString()))
                .andExpect(jsonPath("$[0].ativa").value(true))
                .andExpect(jsonPath("$[1].escolaNome").value("Escola Opcional"));

        RecordedRequest recorded = aguardarRequisicao("GET", "/internal/auth/escolas");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer identity-user-token");
    }

    @Test
    void deveConsultarContextoAtualNoContratoInterno() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        String token = "identity-user-token";

        inserirEscola(escolaId, "Escola Contexto");
        inserirUsuario(usuarioId, "usuario.contexto", escolaId);
        inserirSessao(usuarioId, null, token);

        mockMvc.perform(get("/internal/v1/auth/contexto-atual")
                        .header("X-Internal-Token", "identity-token")
                        .header("X-Correlation-Id", "corr-identity-ctx")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(usuarioId.toString()))
                .andExpect(jsonPath("$.escolaId").value(escolaId.toString()))
                .andExpect(jsonPath("$.escolaNome").value("Escola Contexto"))
                .andExpect(jsonPath("$.username").value("usuario.contexto"));

        assertThat(mockWebServer.takeRequest(500, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test
    void deveAplicarEscolaPadraoQuandoSessaoEUsuarioNaoPossuemEscola() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        String token = "identity-user-token-default";

        inserirUsuario(usuarioId, "usuario.default", null);
        inserirSessao(usuarioId, null, token);

        mockMvc.perform(get("/internal/v1/auth/contexto-atual")
                        .header("X-Internal-Token", "identity-token")
                        .header("X-Correlation-Id", "corr-identity-ctx-default")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(usuarioId.toString()))
                .andExpect(jsonPath("$.escolaId").value(ESCOLA_PADRAO_ID.toString()))
                .andExpect(jsonPath("$.escolaNome").value("Escola padrao"))
                .andExpect(jsonPath("$.username").value("usuario.default"));

        assertThat(mockWebServer.takeRequest(500, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test
    void deveSelecionarEscolaAtivaNoContratoInterno() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "usuarioId":"%s",
                          "escolaId":"%s",
                          "escolaNome":"Escola Selecionada",
                          "username":"usuario.identity"
                        }
                        """.formatted(usuarioId, escolaId)));

        mockMvc.perform(post("/internal/v1/auth/escola-ativa")
                        .contentType("application/json")
                        .content("""
                                {
                                  "escolaId":"%s"
                                }
                                """.formatted(escolaId))
                        .header("X-Internal-Token", "identity-token")
                        .header("X-Correlation-Id", "corr-identity-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer identity-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(usuarioId.toString()))
                .andExpect(jsonPath("$.escolaId").value(escolaId.toString()))
                .andExpect(jsonPath("$.username").value("usuario.identity"));

        RecordedRequest recorded = aguardarRequisicao("POST", "/internal/auth/escola-ativa");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer identity-user-token");
        assertThat(recorded.getBody().readUtf8()).contains(escolaId.toString());
    }

    @Test
    void deveExigirTokenInternoValido() throws Exception {
        mockMvc.perform(get("/internal/v1/auth/escolas")
                        .header("X-Correlation-Id", "corr-identity-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer identity-user-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INTERNAL_UNAUTHORIZED"));
    }

    private RecordedRequest aguardarRequisicao(String method, String path) throws InterruptedException {
        RecordedRequest recorded = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        assertThat(recorded.getMethod()).isEqualTo(method);
        assertThat(recorded.getPath()).isEqualTo(path);
        return recorded;
    }

    private void inserirEscola(UUID escolaId, String nome) {
        jdbcTemplate.update("""
                INSERT INTO escola (id_escola, nome, ativo, created_at)
                VALUES (?, ?, ?, ?)
                """, escolaId, nome, true, Timestamp.valueOf(LocalDateTime.now()));
    }

    private void inserirUsuario(UUID usuarioId, String username, UUID escolaId) {
        jdbcTemplate.update("""
                INSERT INTO usuario (id_usuario, username, nome, email, senha_hash, ativo, created_at, id_escola)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                usuarioId,
                username,
                "Nome " + username,
                username + "@escola.com",
                "hash",
                true,
                Timestamp.valueOf(LocalDateTime.now()),
                escolaId);
    }

    private void inserirSessao(UUID usuarioId, UUID escolaId, String token) {
        jdbcTemplate.update("""
                INSERT INTO sessao_autenticacao (
                    id_sessao_autenticacao,
                    id_usuario,
                    id_escola,
                    refresh_token_hash,
                    access_token_hash,
                    expira_em,
                    access_expira_em,
                    revogado,
                    created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                usuarioId,
                escolaId,
                "refresh",
                hashToken(token),
                Timestamp.valueOf(LocalDateTime.now().plusDays(1)),
                Timestamp.valueOf(LocalDateTime.now().plusMinutes(30)),
                false,
                Timestamp.valueOf(LocalDateTime.now()));
    }

    private String hashToken(String token) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}

