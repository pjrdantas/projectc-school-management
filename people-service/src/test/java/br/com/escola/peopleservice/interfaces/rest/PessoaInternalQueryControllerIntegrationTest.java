package br.com.escola.peopleservice.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.sql.DriverManager;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import br.com.escola.peopleservice.application.service.PeopleReadModelSyncState;
import br.com.escola.peopleservice.application.service.PessoaContatoService;
import br.com.escola.peopleservice.application.service.PessoaEnderecoService;
import br.com.escola.peopleservice.application.dto.PessoaContatoResponse;
import br.com.escola.peopleservice.application.state.PeopleReadModelSyncSummary;
import br.com.escola.peopleservice.application.dto.PessoaEnderecoResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@SpringBootTest
@AutoConfigureMockMvc
class PessoaInternalQueryControllerIntegrationTest {

    private static final String READ_MODEL_URL = "jdbc:h2:mem:people-internal-query;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    private static final UUID ESCOLA_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");
    private static final UUID PESSOA_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static MockWebServer mockWebServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PeopleReadModelSyncState peopleReadModelSyncState;

    @MockBean
    private PessoaEnderecoService pessoaEnderecoService;

    @MockBean
    private PessoaContatoService pessoaContatoService;

    @BeforeAll
    static void beforeAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        prepararCatalogosLocais();
    }

    @AfterAll
    static void afterAll() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("people.internal-api.token", () -> "internal-token");
        registry.add("people.monolith.base-url", () -> mockWebServer.url("/").toString());
        registry.add("people.read-model.enabled", () -> true);
        registry.add("people.read-model.local-read-routing-enabled", () -> true);
        registry.add("people.read-model.backfill-enabled", () -> true);
        registry.add("people.read-model.reconciliation-enabled", () -> true);
        registry.add("people.read-model.fallback-enabled", () -> true);
        registry.add("people.read-model.schema-migration.driver-class-name", () -> "org.h2.Driver");
        registry.add("people.read-model.schema-migration.url", () -> READ_MODEL_URL);
        registry.add("people.read-model.schema-migration.username", () -> "sa");
        registry.add("people.read-model.schema-migration.password", () -> "");
    }

    @Test
    void deveConsultarPessoaPorIdNoRuntimeInterno() throws Exception {
        marcarReadModelComoVerde();
        UUID pessoaId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id": "%s",
                          "nomeCompleto": "Pessoa Interna",
                          "escolaId": "%s",
                          "escolaNome": "Escola Padrao",
                          "ativo": true
                        }
                        """.formatted(pessoaId, ESCOLA_ID)));

        mockMvc.perform(get("/internal/v1/pessoas/{id}", pessoaId)
                        .header("X-Internal-Token", "internal-token")
                        .header("X-Correlation-Id", "corr-people-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer internal-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pessoaId.toString()))
                .andExpect(jsonPath("$.nomeCompleto").value("Pessoa Interna"));

        RecordedRequest recorded = aguardarRequisicao("GET", "/internal/pessoas/" + pessoaId);
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer internal-user-token");
        assertThat(recorded.getHeader("X-Escola-Id")).isEqualTo(ESCOLA_ID.toString());
        assertThat(recorded.getHeader("X-Correlation-Id")).isEqualTo("corr-people-1");
    }

    @Test
    void deveExporRotasInternasCompativeisEConsultaCadastral() throws Exception {
        marcarReadModelComoVerde();
        UUID tipoPessoaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id": "%s",
                            "codigo": "ALUNO",
                            "descricao": "Aluno"
                          }
                        ]
                        """.formatted(tipoPessoaId)));
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "content": [
                            {
                              "idAluno": "%s",
                              "nomeCompleto": "Aluno Interno",
                              "cpf": "12345678901",
                              "email": "aluno.internal@example.com",
                              "telefone": "11999999999",
                              "dataNascimento": "2014-03-10",
                              "createdAt": "2026-07-02T08:00:00",
                              "responsaveis": [
                                {
                                  "id": "%s",
                                  "nomeCompleto": "Responsavel Interno",
                                  "cpf": "98765432100",
                                  "email": "responsavel.internal@example.com",
                                  "telefone": "11888888888",
                                  "createdAt": "2026-07-02T08:30:00"
                                }
                              ]
                            }
                          ],
                          "totalElements": 1,
                          "page": 0,
                          "size": 10
                        }
                        """.formatted(alunoId, responsavelId)));

        mockMvc.perform(get("/internal/pessoas/catalogos/tipos-pessoa")
                        .header("X-Internal-Token", "internal-token")
                        .header("X-Correlation-Id", "corr-people-2a")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer internal-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(tipoPessoaId.toString()))
                .andExpect(jsonPath("$[0].codigo").value("ALUNO"));

        mockMvc.perform(get("/internal/v1/pessoas/consulta-cadastral")
                        .queryParam("nomeAluno", "Aluno")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .header("X-Internal-Token", "internal-token")
                        .header("X-Correlation-Id", "corr-people-2b")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer internal-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].idAluno").value(alunoId.toString()))
                .andExpect(jsonPath("$.content[0].responsaveis[0].id").value(responsavelId.toString()));

        RecordedRequest catalogoRequest = aguardarRequisicao("GET", "/internal/pessoas/catalogos/tipos-pessoa");
        assertThat(catalogoRequest.getPath()).isEqualTo("/internal/pessoas/catalogos/tipos-pessoa");

        RecordedRequest consultaRequest = aguardarRequisicao("GET", "/internal/pessoas/consulta-cadastral?nomeAluno=Aluno&page=0&size=10");
        assertThat(consultaRequest.getPath()).isEqualTo("/internal/pessoas/consulta-cadastral?nomeAluno=Aluno&page=0&size=10");
    }

    @Test
    void deveExigirTokenInternoValido() throws Exception {
        mockMvc.perform(get("/internal/v1/pessoas/catalogos/tipos-pessoa")
                        .header("X-Correlation-Id", "corr-people-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer internal-user-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INTERNAL_UNAUTHORIZED"));
    }

    @Test
    void deveMapearPessoaNaoEncontrada() throws Exception {
        marcarReadModelComoVerde();
        UUID pessoaId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        mockMvc.perform(get("/internal/v1/pessoas/{id}", pessoaId)
                        .header("X-Internal-Token", "internal-token")
                        .header("X-Correlation-Id", "corr-people-4")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer internal-user-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void deveExporCatalogosLocaisDeStatusAlunoEParentesco() throws Exception {
        marcarReadModelComoVerde();

        mockMvc.perform(get("/internal/v1/pessoas/catalogos/status-aluno")
                        .header("X-Internal-Token", "internal-token")
                        .header("X-Correlation-Id", "corr-people-5a")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer internal-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/internal/v1/pessoas/catalogos/parentescos")
                        .header("X-Internal-Token", "internal-token")
                        .header("X-Correlation-Id", "corr-people-5b")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer internal-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void deveExporLeiturasInternasDeEndereco() throws Exception {
        marcarReadModelComoVerde();
        when(pessoaEnderecoService.buscarEnderecoPrincipalPorPessoa(PESSOA_ID, ESCOLA_ID))
                .thenReturn(java.util.Optional.of(enderecoPrincipal()));
        when(pessoaEnderecoService.listarEnderecosPorPessoa(PESSOA_ID, ESCOLA_ID))
                .thenReturn(List.of(enderecoPrincipal(), enderecoSecundario()));

        mockMvc.perform(get("/internal/v1/pessoas/{id}/endereco-principal", PESSOA_ID)
                        .header("X-Internal-Token", "internal-token")
                        .header("X-Correlation-Id", "corr-people-6a")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer internal-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pessoaId").value(PESSOA_ID.toString()))
                .andExpect(jsonPath("$.cep").value("01001000"))
                .andExpect(jsonPath("$.principal").value(true));

        mockMvc.perform(get("/internal/v1/pessoas/{id}/enderecos", PESSOA_ID)
                        .header("X-Internal-Token", "internal-token")
                        .header("X-Correlation-Id", "corr-people-6b")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer internal-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pessoaId").value(PESSOA_ID.toString()))
                .andExpect(jsonPath("$[0].cep").value("01001000"));
    }

    @Test
    void deveExporLeituraInternaDeContato() throws Exception {
        marcarReadModelComoVerde();
        when(pessoaContatoService.buscarContatoPorPessoa(PESSOA_ID, ESCOLA_ID))
                .thenReturn(java.util.Optional.of(contato()));

        mockMvc.perform(get("/internal/v1/pessoas/{id}/contato", PESSOA_ID)
                        .header("X-Internal-Token", "internal-token")
                        .header("X-Correlation-Id", "corr-people-7")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer internal-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pessoaId").value(PESSOA_ID.toString()))
                .andExpect(jsonPath("$.email").value("ana.aluna@example.com"))
                .andExpect(jsonPath("$.telefone").value("11999999999"));
    }

    private void marcarReadModelComoVerde() {
        peopleReadModelSyncState.update(new PeopleReadModelSyncSummary(
                true,
                true,
                "completed",
                "local-read-model-backfill-and-reconciliation-completed",
                500,
                4,
                4,
                12,
                12,
                12,
                0,
                false,
                false,
                List.of()));
    }

    private static void prepararCatalogosLocais() {
        try (var connection = DriverManager.getConnection(READ_MODEL_URL);
                var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS status_aluno (
                        id_status_aluno UUID PRIMARY KEY,
                        codigo VARCHAR(64),
                        descricao VARCHAR(255)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS parentesco (
                        id_parentesco UUID PRIMARY KEY,
                        codigo VARCHAR(64),
                        descricao VARCHAR(255)
                    )
                    """);
            statement.execute("DELETE FROM status_aluno");
            statement.execute("DELETE FROM parentesco");
            statement.execute("""
                    INSERT INTO status_aluno (id_status_aluno, codigo, descricao)
                    VALUES ('11111111-1111-1111-1111-111111111111', 'ATIVO', 'Ativo')
                    """);
            statement.execute("""
                    INSERT INTO parentesco (id_parentesco, codigo, descricao)
                    VALUES ('22222222-2222-2222-2222-222222222222', 'MAE', 'Mae')
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS pessoa (
                        id_pessoa UUID NOT NULL PRIMARY KEY,
                        id_escola UUID NOT NULL,
                        nome_completo VARCHAR(150) NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS tipo_endereco (
                        id_tipo_endereco UUID NOT NULL PRIMARY KEY,
                        codigo VARCHAR(50) NOT NULL,
                        descricao VARCHAR(150) NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS endereco (
                        id_endereco UUID NOT NULL PRIMARY KEY,
                        cep VARCHAR(20),
                        logradouro VARCHAR(150),
                        numero VARCHAR(30),
                        complemento VARCHAR(150),
                        bairro VARCHAR(100),
                        cidade VARCHAR(100),
                        uf VARCHAR(2),
                        created_at TIMESTAMP,
                        updated_at TIMESTAMP
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS pessoa_endereco (
                        id_pessoa_endereco UUID NOT NULL PRIMARY KEY,
                        id_pessoa UUID NOT NULL,
                        id_endereco UUID NOT NULL,
                        id_tipo_endereco UUID NOT NULL,
                        principal BOOLEAN NOT NULL,
                        created_at TIMESTAMP NOT NULL
                    )
                    """);
            statement.execute("DELETE FROM pessoa_endereco");
            statement.execute("DELETE FROM endereco");
            statement.execute("DELETE FROM tipo_endereco");
            statement.execute("DELETE FROM pessoa");
            statement.execute("""
                    INSERT INTO pessoa (id_pessoa, id_escola, nome_completo)
                    VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '00000000-0000-0000-0000-000000000047', 'Ana Aluna')
                    """);
            statement.execute("""
                    INSERT INTO tipo_endereco (id_tipo_endereco, codigo, descricao)
                    VALUES ('11111111-1111-1111-1111-111111111111', 'RESIDENCIAL', 'Residencial')
                    """);
            statement.execute("""
                    INSERT INTO endereco (
                        id_endereco, cep, logradouro, numero, complemento, bairro, cidade, uf, created_at, updated_at
                    ) VALUES
                    (
                        '22222222-2222-2222-2222-222222222222',
                        '01001000',
                        'Praca da Se',
                        '100',
                        'Apto 1',
                        'Se',
                        'Sao Paulo',
                        'SP',
                        TIMESTAMP '2026-01-02 10:00:00',
                        TIMESTAMP '2026-01-02 10:00:00'
                    ),
                    (
                        '33333333-3333-3333-3333-333333333333',
                        '20040002',
                        'Rua da Assembleia',
                        '200',
                        NULL,
                        'Centro',
                        'Rio de Janeiro',
                        'RJ',
                        TIMESTAMP '2026-01-03 10:00:00',
                        TIMESTAMP '2026-01-03 10:00:00'
                    )
                    """);
            statement.execute("""
                    INSERT INTO pessoa_endereco (
                        id_pessoa_endereco, id_pessoa, id_endereco, id_tipo_endereco, principal, created_at
                    ) VALUES
                    (
                        '44444444-4444-4444-4444-444444444444',
                        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
                        '22222222-2222-2222-2222-222222222222',
                        '11111111-1111-1111-1111-111111111111',
                        TRUE,
                        TIMESTAMP '2026-01-04 10:00:00'
                    ),
                    (
                        '55555555-5555-5555-5555-555555555555',
                        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
                        '33333333-3333-3333-3333-333333333333',
                        '11111111-1111-1111-1111-111111111111',
                        FALSE,
                        TIMESTAMP '2026-01-03 10:00:00'
                    )
                    """);
        } catch (Exception ex) {
            throw new IllegalStateException("failed-to-prepare-local-catalogs", ex);
        }
    }

    private PessoaEnderecoResponse enderecoPrincipal() {
        return new PessoaEnderecoResponse(
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                PESSOA_ID,
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "RESIDENCIAL",
                "Residencial",
                true,
                "01001000",
                "Praca da Se",
                "100",
                "Apto 1",
                "Se",
                "Sao Paulo",
                "SP");
    }

    private PessoaEnderecoResponse enderecoSecundario() {
        return new PessoaEnderecoResponse(
                UUID.fromString("55555555-5555-5555-5555-555555555555"),
                PESSOA_ID,
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "RESIDENCIAL",
                "Residencial",
                false,
                "20040002",
                "Rua da Assembleia",
                "200",
                null,
                "Centro",
                "Rio de Janeiro",
                "RJ");
    }

    private PessoaContatoResponse contato() {
        return new PessoaContatoResponse(
                PESSOA_ID,
                ESCOLA_ID,
                "ana.aluna@example.com",
                "11999999999",
                true);
    }

    private RecordedRequest aguardarRequisicao(String method, String path) throws InterruptedException {
        for (int tentativa = 0; tentativa < 5; tentativa++) {
            RecordedRequest request = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
            if (request == null) {
                continue;
            }
            if (method.equals(request.getMethod()) && path.equals(request.getPath())) {
                return request;
            }
        }
        throw new AssertionError("Requisicao esperada nao encontrada: " + method + " " + path);
    }
}

