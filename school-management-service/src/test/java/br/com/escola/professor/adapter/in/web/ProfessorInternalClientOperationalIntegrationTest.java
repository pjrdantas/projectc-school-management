package br.com.escola.professor.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "professor.internal-client.enabled=true",
                "professor.internal-client.fallback-local-on-error=false",
                "professor.internal-client.base-url=http://localhost:${local.server.port}"
        })
@Sql(
        statements = {
                "DELETE FROM sessao_autenticacao WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'professor-internal-client-%')",
                "DELETE FROM usuario_perfil WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'professor-internal-client-%')",
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'professor-internal-client-%')",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'professor.internal.client.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'PROF-INT-CLIENT-%'",
                "DELETE FROM usuario WHERE username LIKE 'professor-internal-client-%'",
                "DELETE FROM pessoa WHERE email LIKE 'professor.internal.client.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'PROF-INT-CLIENT-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Professor Internal Client %'",
                "DELETE FROM turma WHERE codigo LIKE 'PROF-INT-CLIENT-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'PROF-INT-CLIENT-%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM sessao_autenticacao WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'professor-internal-client-%')",
                "DELETE FROM usuario_perfil WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'professor-internal-client-%')",
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'professor-internal-client-%')",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'professor.internal.client.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'PROF-INT-CLIENT-%'",
                "DELETE FROM usuario WHERE username LIKE 'professor-internal-client-%'",
                "DELETE FROM pessoa WHERE email LIKE 'professor.internal.client.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'PROF-INT-CLIENT-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Professor Internal Client %'",
                "DELETE FROM turma WHERE codigo LIKE 'PROF-INT-CLIENT-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'PROF-INT-CLIENT-%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ProfessorInternalClientOperationalIntegrationTest {

    private static final UUID SERIE_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void deveConsumirClienteInternoEmFluxoAutenticadoPontaAPonta() throws Exception {
        String username = "professor-internal-client-" + System.nanoTime();
        criarUsuario(username, "senha123");
        String accessToken = autenticar(username, "senha123");

        UUID funcionarioId = criarFuncionario(
                "Professor Internal Client Fluxo",
                "professor.internal.client.fluxo@example.com");
        UUID periodoId = criarPeriodo(accessToken, "PROF-INT-CLIENT-2040.1", "2040-02-01", "2040-06-30");
        UUID turmaId = criarTurma(accessToken, "PROF-INT-CLIENT-A", "Professor Internal Client Turma A", 30, periodoId);
        UUID disciplinaId = criarDisciplina(accessToken, "Professor Internal Client Matemática", 80);
        UUID turmaDisciplinaId = vincularDisciplina(accessToken, turmaId, disciplinaId, 80);

        double criarAntes = contador("criar", "internal", "success");
        double listarProfessoresAntes = contador("listar", "internal", "success");
        double buscarAntes = contador("buscarPorId", "internal", "success");
        double alocarAntes = contador("vincularTurmaDisciplina", "internal", "success");
        double listarAntes = contador("listarAlocacoes", "internal", "success");
        double listarPorTurmaAntes = contador("listarPorTurma", "internal", "success");
        double listarElegiveisAntes = contador("listarFuncionariosElegiveis", "internal", "success");
        double fallbackAntes = contadorFallback("criar", "RestClientException")
                + contadorFallback("listar", "RestClientException")
                + contadorFallback("buscarPorId", "RestClientException")
                + contadorFallback("vincularTurmaDisciplina", "RestClientException")
                + contadorFallback("listarAlocacoes", "RestClientException")
                + contadorFallback("listarPorTurma", "RestClientException")
                + contadorFallback("listarFuncionariosElegiveis", "RestClientException");

        RestClient client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .defaultHeader("X-Correlation-Id", "professor-internal-client-operational")
                .build();

        String professorResponse = client.post()
                .uri("/api/professores")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "funcionarioId": "%s",
                          "registroProfissional": "RP-2040",
                          "formacao": "Licenciatura em Matemática"
                        }
                        """.formatted(funcionarioId))
                .retrieve()
                .body(String.class);

        JsonNode professorJson = objectMapper.readTree(professorResponse);
        UUID professorId = UUID.fromString(professorJson.get("id").asText());
        assertThat(professorJson.get("nomeCompleto").asText()).isEqualTo("Professor Internal Client Fluxo");

        String professoresResponse = client.get()
                .uri("/api/professores")
                .retrieve()
                .body(String.class);
        JsonNode professoresJson = objectMapper.readTree(professoresResponse);
        assertThat(professoresJson).hasSize(1);
        assertThat(professoresJson.get(0).get("id").asText()).isEqualTo(professorId.toString());

        String elegiveisResponse = client.get()
                .uri("/api/professores/funcionarios-elegiveis")
                .retrieve()
                .body(String.class);
        JsonNode elegiveisJson = objectMapper.readTree(elegiveisResponse);
        assertThat(elegiveisJson.isArray()).isTrue();
        assertThat(elegiveisJson).isEmpty();

        String professorConsulta = client.get()
                .uri("/api/professores/{id}", professorId)
                .retrieve()
                .body(String.class);
        assertThat(objectMapper.readTree(professorConsulta).get("id").asText()).isEqualTo(professorId.toString());

        String alocacaoResponse = client.post()
                .uri("/api/professores/{id}/turmas-disciplinas", professorId)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "turmaDisciplinaId": "%s",
                          "dataInicio": "2040-02-01"
                        }
                        """.formatted(turmaDisciplinaId))
                .retrieve()
                .body(String.class);

        JsonNode alocacaoJson = objectMapper.readTree(alocacaoResponse);
        assertThat(alocacaoJson.get("professorId").asText()).isEqualTo(professorId.toString());
        assertThat(alocacaoJson.get("turmaId").asText()).isEqualTo(turmaId.toString());
        assertThat(alocacaoJson.get("disciplinaId").asText()).isEqualTo(disciplinaId.toString());

        String listaResponse = client.get()
                .uri("/api/professores/{id}/turmas-disciplinas", professorId)
                .retrieve()
                .body(String.class);
        JsonNode listaJson = objectMapper.readTree(listaResponse);
        assertThat(listaJson).hasSize(1);
        assertThat(listaJson.get(0).get("professorId").asText()).isEqualTo(professorId.toString());

        String turmaProfessoresResponse = client.get()
                .uri("/api/turmas/{turmaId}/professores", turmaId)
                .retrieve()
                .body(String.class);
        JsonNode turmaProfessoresJson = objectMapper.readTree(turmaProfessoresResponse);
        assertThat(turmaProfessoresJson).hasSize(1);
        assertThat(turmaProfessoresJson.get(0).get("professorId").asText()).isEqualTo(professorId.toString());
        assertThat(turmaProfessoresJson.get(0).get("turmaId").asText()).isEqualTo(turmaId.toString());

        assertThat(contador("criar", "internal", "success") - criarAntes).isEqualTo(1.0d);
        assertThat(contador("listar", "internal", "success") - listarProfessoresAntes).isEqualTo(1.0d);
        assertThat(contador("buscarPorId", "internal", "success") - buscarAntes).isEqualTo(1.0d);
        assertThat(contador("vincularTurmaDisciplina", "internal", "success") - alocarAntes).isEqualTo(1.0d);
        assertThat(contador("listarAlocacoes", "internal", "success") - listarAntes).isEqualTo(1.0d);
        assertThat(contador("listarPorTurma", "internal", "success") - listarPorTurmaAntes).isEqualTo(1.0d);
        assertThat(contador("listarFuncionariosElegiveis", "internal", "success") - listarElegiveisAntes).isEqualTo(1.0d);
        double fallbackDepois = contadorFallback("criar", "RestClientException")
                + contadorFallback("listar", "RestClientException")
                + contadorFallback("buscarPorId", "RestClientException")
                + contadorFallback("vincularTurmaDisciplina", "RestClientException")
                + contadorFallback("listarAlocacoes", "RestClientException")
                + contadorFallback("listarPorTurma", "RestClientException")
                + contadorFallback("listarFuncionariosElegiveis", "RestClientException");
        assertThat(fallbackDepois - fallbackAntes).isEqualTo(0.0d);
    }

    private void criarUsuario(String username, String senha) {
        jdbcTemplate.update("""
                INSERT INTO usuario (id_usuario, username, nome, email, senha_hash, ativo, created_at)
                VALUES (?, ?, ?, ?, ?, true, CURRENT_TIMESTAMP)
                """,
                UUID.randomUUID(),
                username,
                "Professor Internal Client",
                username + "@example.com",
                passwordEncoder.encode(senha));
    }

    private String autenticar(String username, String senha) {
        RestClient client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        Map<?, ?> response = client.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("login", username, "senha", senha))
                .retrieve()
                .body(Map.class);

        assertThat(response).isNotNull();
        return response.get("accessToken").toString();
    }

    private UUID criarFuncionario(String nome, String email) {
        UUID pessoaId = UUID.randomUUID();
        UUID cargoId = UUID.randomUUID();
        UUID funcionarioId = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO pessoa (id_pessoa, nome_completo, cpf, email, id_escola, ativo, created_at)
                VALUES (?, ?, ?, ?, '00000000-0000-0000-0000-000000000047', true, CURRENT_TIMESTAMP)
                """, pessoaId, nome, cpfAleatorio(), email);

        jdbcTemplate.update("""
                INSERT INTO cargo (id_cargo, codigo, descricao)
                VALUES (?, ?, ?)
                """, cargoId, "PROF-INT-CLIENT-" + System.nanoTime(), "Professor");

        jdbcTemplate.update("""
                INSERT INTO funcionario (id_funcionario, id_pessoa, id_cargo, ativo, created_at)
                VALUES (?, ?, ?, true, CURRENT_TIMESTAMP)
                """, funcionarioId, pessoaId, cargoId);

        return funcionarioId;
    }

    private UUID criarPeriodo(String accessToken, String nome, String dataInicio, String dataFim) {
        RestClient client = apiClient(accessToken);
        Map<?, ?> response = client.post()
                .uri("/api/periodos-letivos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "nome", nome,
                        "dataInicio", dataInicio,
                        "dataFim", dataFim))
                .retrieve()
                .body(Map.class);
        return UUID.fromString(response.get("id").toString());
    }

    private UUID criarTurma(String accessToken, String codigo, String nome, int capacidade, UUID periodoId) {
        RestClient client = apiClient(accessToken);
        Map<?, ?> response = client.post()
                .uri("/api/turmas")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "codigo", codigo,
                        "nome", nome,
                        "capacidade", capacidade,
                        "periodoLetivoId", periodoId.toString(),
                        "serieId", SERIE_PADRAO_ID.toString(),
                        "turno", "MANHA",
                        "status", "ATIVA"))
                .retrieve()
                .body(Map.class);
        return UUID.fromString(response.get("id").toString());
    }

    private UUID criarDisciplina(String accessToken, String nome, int cargaHoraria) {
        RestClient client = apiClient(accessToken);
        Map<?, ?> response = client.post()
                .uri("/api/disciplinas")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "nome", nome,
                        "cargaHoraria", cargaHoraria,
                        "status", "ATIVA"))
                .retrieve()
                .body(Map.class);
        return UUID.fromString(response.get("id").toString());
    }

    private UUID vincularDisciplina(String accessToken, UUID turmaId, UUID disciplinaId, int cargaHoraria) {
        RestClient client = apiClient(accessToken);
        Map<?, ?> response = client.post()
                .uri("/api/turmas/{turmaId}/disciplinas", turmaId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "disciplinaId", disciplinaId.toString(),
                        "cargaHoraria", cargaHoraria))
                .retrieve()
                .body(Map.class);
        return UUID.fromString(response.get("id").toString());
    }

    private RestClient apiClient(String accessToken) {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .build();
    }

    private double contador(String operacao, String destino, String resultado) {
        Counter counter = meterRegistry.find("professor.internal.client.requests")
                .tags(
                        "operacao", operacao,
                        "destino", destino,
                        "resultado", resultado)
                .counter();
        return counter == null ? 0.0d : counter.count();
    }

    private double contadorFallback(String operacao, String causa) {
        Counter counter = meterRegistry.find("professor.internal.client.fallbacks")
                .tags(
                        "operacao", operacao,
                        "causa", causa)
                .counter();
        return counter == null ? 0.0d : counter.count();
    }

    private String cpfAleatorio() {
        long cpf = System.nanoTime() % 1_000_000_00000L;
        return String.format("%011d", cpf);
    }
}
