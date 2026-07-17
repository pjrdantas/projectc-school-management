package br.com.escola.responsiblesservice.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest
@AutoConfigureMockMvc
class LeituraModeloOperationalDivergentIntegrationTest {

    private static final String SOURCE_URL = "jdbc:h2:mem:responsibles_operational_divergent_source;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    private static final String TARGET_URL = "jdbc:h2:mem:responsibles_operational_divergent_target;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";

    private static MockWebServer mockWebServer;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void beforeAll() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        criarSchemaOrigem();
        criarSchemaDestinoDivergente();
    }

    @AfterAll
    static void afterAll() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("responsibles.internal-api.token", () -> "responsibles-token");
        registry.add("responsibles.monolith.base-url", () -> mockWebServer.url("/").toString());
        registry.add("responsibles.read-model.enabled", () -> "true");
        registry.add("responsibles.read-model.migration-enabled", () -> "false");
        registry.add("responsibles.read-model.local-read-routing-enabled", () -> "true");
        registry.add("responsibles.read-model.backfill-enabled", () -> "false");
        registry.add("responsibles.read-model.reconciliation-enabled", () -> "true");
        registry.add("responsibles.read-model.fail-on-error", () -> "false");
        registry.add("responsibles.read-model.schema-migration.url", () -> TARGET_URL);
        registry.add("responsibles.read-model.schema-migration.username", () -> "sa");
        registry.add("responsibles.read-model.schema-migration.password", () -> "");
        registry.add("responsibles.read-model.schema-migration.driver-class-name", () -> "org.h2.Driver");
        registry.add("responsibles.read-model.source.source-url", () -> SOURCE_URL);
        registry.add("responsibles.read-model.source.source-username", () -> "sa");
        registry.add("responsibles.read-model.source.source-password", () -> "");
        registry.add("responsibles.read-model.source.source-driver-class-name", () -> "org.h2.Driver");
        registry.add("management.endpoint.health.show-details", () -> "always");
    }

    @Test
    void deveExporHealthUnknownQuandoReconciliacaoEstiverDivergente() throws Exception {
        mockMvc.perform(get("/actuator/health/responsiblesLocalPersistence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNKNOWN"))
                .andExpect(jsonPath("$.details.catalogRouteReady").value(false))
                .andExpect(jsonPath("$.details.linkRouteReady").value(false))
                .andExpect(jsonPath("$.details.lastStatus").value("blocked"))
                .andExpect(jsonPath("$.details.divergentRecords").value(3));
    }

    @Test
    void deveFazerFallbackParaMonolitoNoCatalogoQuandoGateNaoEstiverVerde() throws Exception {
        UUID responsavelId = UUID.fromString("00000000-0000-0000-0000-000000000601");
        UUID usuarioId = UUID.fromString("00000000-0000-0000-0000-000000000101");
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000601",
                          "nomeCompleto":"Monolito Lista"
                        }]
                        """));
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id":"00000000-0000-0000-0000-000000000601",
                          "nomeCompleto":"Monolito Detalhe"
                        }
                        """));

        mockMvc.perform(get("/internal/v1/responsaveis")
                        .param("nome", "Maria")
                        .header("Authorization", "Bearer opaque-token")
                        .header("X-Internal-Token", "responsibles-token")
                        .header("X-Correlation-Id", "corr-divergent-list-1")
                        .header("X-Usuario-Id", usuarioId)
                        .header("X-Escola-Id", escolaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeCompleto").value("Monolito Lista"));

        mockMvc.perform(get("/internal/v1/responsaveis/{id}", responsavelId)
                        .header("Authorization", "Bearer opaque-token")
                        .header("X-Internal-Token", "responsibles-token")
                        .header("X-Correlation-Id", "corr-divergent-detail-1")
                        .header("X-Usuario-Id", usuarioId)
                        .header("X-Escola-Id", escolaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCompleto").value("Monolito Detalhe"));

        assertThat(mockWebServer.takeRequest().getPath()).isEqualTo("/api/responsaveis?nome=Maria");
        assertThat(mockWebServer.takeRequest().getPath()).isEqualTo("/api/responsaveis/" + responsavelId);
    }

    @Test
    void deveFazerFallbackParaMonolitoQuandoGateNaoEstiverVerde() throws Exception {
        UUID alunoId = UUID.fromString("00000000-0000-0000-0000-000000000401");
        UUID usuarioId = UUID.fromString("00000000-0000-0000-0000-000000000101");
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000601",
                          "nomeCompleto":"Monolito Fallback",
                          "parentesco":"MAE"
                        }]
                        """));

        mockMvc.perform(get("/internal/v1/alunos/{alunoId}/responsaveis", alunoId)
                        .header("Authorization", "Bearer opaque-token")
                        .header("X-Internal-Token", "responsibles-token")
                        .header("X-Correlation-Id", "corr-divergent-1")
                        .header("X-Usuario-Id", usuarioId)
                        .header("X-Escola-Id", escolaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeCompleto").value("Monolito Fallback"));

        assertThat(mockWebServer.takeRequest().getPath()).isEqualTo("/api/alunos/" + alunoId + "/responsaveis");
    }

    private static void criarSchemaOrigem() throws SQLException {
        try (var connection = DriverManager.getConnection(SOURCE_URL, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE escola (id_escola UUID PRIMARY KEY, nome VARCHAR(150) NOT NULL)");
            statement.execute("""
                    CREATE TABLE pessoa (
                        id_pessoa UUID PRIMARY KEY,
                        nome_completo VARCHAR(150) NOT NULL,
                        cpf VARCHAR(14),
                        email VARCHAR(150),
                        telefone VARCHAR(20),
                        rg VARCHAR(20),
                        id_escola UUID NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE responsavel (
                        id_responsavel UUID PRIMARY KEY,
                        id_pessoa UUID NOT NULL,
                        created_at TIMESTAMP NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE parentesco (
                        id_parentesco UUID PRIMARY KEY,
                        codigo VARCHAR(40) NOT NULL,
                        descricao VARCHAR(120) NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE aluno_responsavel (
                        id_aluno_responsavel UUID PRIMARY KEY,
                        id_aluno UUID NOT NULL,
                        id_responsavel UUID NOT NULL,
                        id_parentesco UUID,
                        responsavel_financeiro BOOLEAN NOT NULL,
                        responsavel_pedagogico BOOLEAN NOT NULL,
                        autorizado_retirar BOOLEAN NOT NULL,
                        created_at TIMESTAMP NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE endereco (
                        id_endereco UUID PRIMARY KEY,
                        cep VARCHAR(14),
                        logradouro VARCHAR(200),
                        numero VARCHAR(20),
                        complemento VARCHAR(120),
                        bairro VARCHAR(120),
                        cidade VARCHAR(120),
                        uf VARCHAR(2)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE pessoa_endereco (
                        id_pessoa_endereco UUID PRIMARY KEY,
                        id_pessoa UUID NOT NULL,
                        id_endereco UUID NOT NULL,
                        principal BOOLEAN NOT NULL
                    )
                    """);
            statement.execute("""
                    INSERT INTO escola (id_escola, nome) VALUES
                    ('00000000-0000-0000-0000-000000000047', 'Escola padrao')
                    """);
            statement.execute("""
                    INSERT INTO pessoa (id_pessoa, nome_completo, cpf, email, telefone, rg, id_escola) VALUES
                    ('00000000-0000-0000-0000-000000000701', 'Maria Souza', '98765432100', 'maria@example.com', '11988887777', '1234567', '00000000-0000-0000-0000-000000000047')
                    """);
            statement.execute("""
                    INSERT INTO responsavel (id_responsavel, id_pessoa, created_at) VALUES
                    ('00000000-0000-0000-0000-000000000601', '00000000-0000-0000-0000-000000000701', TIMESTAMP '2026-07-16 10:00:00')
                    """);
            statement.execute("""
                    INSERT INTO parentesco (id_parentesco, codigo, descricao) VALUES
                    ('00000000-0000-0000-0000-000000000301', 'MAE', 'Mae')
                    """);
            statement.execute("""
                    INSERT INTO aluno_responsavel (
                        id_aluno_responsavel, id_aluno, id_responsavel, id_parentesco,
                        responsavel_financeiro, responsavel_pedagogico, autorizado_retirar, created_at
                    ) VALUES (
                        '00000000-0000-0000-0000-000000000501',
                        '00000000-0000-0000-0000-000000000401',
                        '00000000-0000-0000-0000-000000000601',
                        '00000000-0000-0000-0000-000000000301',
                        TRUE,
                        FALSE,
                        TRUE,
                        TIMESTAMP '2026-07-16 10:00:00'
                    )
                    """);
            statement.execute("""
                    INSERT INTO endereco (id_endereco, cep, logradouro, numero, complemento, bairro, cidade, uf) VALUES
                    ('00000000-0000-0000-0000-000000000801', '01001000', 'Rua Central', '100', 'Casa', 'Centro', 'Sao Paulo', 'SP')
                    """);
            statement.execute("""
                    INSERT INTO pessoa_endereco (id_pessoa_endereco, id_pessoa, id_endereco, principal) VALUES
                    ('00000000-0000-0000-0000-000000000901', '00000000-0000-0000-0000-000000000701', '00000000-0000-0000-0000-000000000801', TRUE)
                    """);
        }
    }

    private static void criarSchemaDestinoDivergente() throws SQLException {
        try (var connection = DriverManager.getConnection(TARGET_URL, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE responsavel (
                        id_responsavel UUID NOT NULL PRIMARY KEY,
                        nome_completo VARCHAR(150) NOT NULL,
                        cpf VARCHAR(14),
                        email VARCHAR(150),
                        telefone VARCHAR(20),
                        rg VARCHAR(20),
                        cep VARCHAR(14),
                        logradouro VARCHAR(200),
                        numero VARCHAR(20),
                        complemento VARCHAR(120),
                        bairro VARCHAR(120),
                        cidade VARCHAR(120),
                        uf VARCHAR(2),
                        id_escola UUID NOT NULL,
                        escola_nome VARCHAR(150),
                        created_at TIMESTAMP NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE parentesco (
                        id_parentesco UUID NOT NULL PRIMARY KEY,
                        codigo VARCHAR(40) NOT NULL,
                        descricao VARCHAR(120) NOT NULL,
                        CONSTRAINT uk_parentesco_codigo UNIQUE (codigo)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE aluno_responsavel (
                        id_aluno_responsavel UUID NOT NULL PRIMARY KEY,
                        id_aluno UUID NOT NULL,
                        id_responsavel UUID NOT NULL,
                        id_parentesco UUID,
                        responsavel_financeiro BOOLEAN NOT NULL DEFAULT FALSE,
                        responsavel_pedagogico BOOLEAN NOT NULL DEFAULT FALSE,
                        autorizado_retirar BOOLEAN NOT NULL DEFAULT FALSE,
                        created_at TIMESTAMP NOT NULL,
                        CONSTRAINT fk_responsavel FOREIGN KEY (id_responsavel) REFERENCES responsavel(id_responsavel),
                        CONSTRAINT uk_aluno_responsavel UNIQUE (id_aluno, id_responsavel)
                    )
                    """);
        }
    }
}

