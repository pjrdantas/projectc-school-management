package br.com.escola.catalog.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import br.com.escola.catalog.domain.model.Disciplina;
import br.com.escola.catalog.application.context.InternalHeaders;
import br.com.escola.catalog.domain.model.PeriodoLetivo;
import br.com.escola.catalog.domain.model.Serie;
import br.com.escola.catalog.domain.model.Turma;
import br.com.escola.catalog.domain.model.TurmaDisciplina;
import br.com.escola.catalog.domain.repository.DisciplinaRepository;
import br.com.escola.catalog.domain.repository.PeriodoLetivoRepository;
import br.com.escola.catalog.domain.repository.SerieRepository;
import br.com.escola.catalog.domain.repository.TurmaDisciplinaRepository;
import br.com.escola.catalog.domain.repository.TurmaRepository;
import br.com.escola.catalog.domain.valueobject.EscolaId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "management.health.redis.enabled=false",
        "catalog.internal-api.token=test-internal-token"
})
class PersistenciaIT {

    private static final UUID NIVEL_ENSINO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000202");
    private static final UUID TURNO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000211");
    private static final EscolaId ESCOLA_A = new EscolaId(
            UUID.fromString("00000000-0000-0000-0000-0000000000a1"));
    private static final EscolaId ESCOLA_B = new EscolaId(
            UUID.fromString("00000000-0000-0000-0000-0000000000b1"));
    private static final UUID USUARIO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private PeriodoLetivoRepository periodoRepository;

    @Autowired
    private SerieRepository serieRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private TurmaDisciplinaRepository turmaDisciplinaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM turma_disciplina");
        jdbcTemplate.update("DELETE FROM turma");
        jdbcTemplate.update("DELETE FROM disciplina");
        jdbcTemplate.update("DELETE FROM serie");
        jdbcTemplate.update("DELETE FROM periodo_letivo");
        jdbcTemplate.update("DELETE FROM outbox_event");
        jdbcTemplate.update("DELETE FROM command_idempotency");
    }

    @Test
    void deveExecutarMigrationsEIsolarDisciplinasDeDuasEscolas() {
        LocalDateTime now = LocalDateTime.now();
        Disciplina disciplinaA = new Disciplina(
                UUID.randomUUID(), ESCOLA_A, "Matematica", 80, true, now);
        Disciplina disciplinaB = new Disciplina(
                UUID.randomUUID(), ESCOLA_B, "Matematica", 60, true, now);

        disciplinaRepository.salvar(disciplinaA);
        disciplinaRepository.salvar(disciplinaB);

        assertThat(disciplinaRepository.listarDisciplinas(ESCOLA_A))
                .extracting(Disciplina::id).containsExactly(disciplinaA.id());
        assertThat(disciplinaRepository.listarDisciplinas(ESCOLA_B))
                .extracting(Disciplina::id).containsExactly(disciplinaB.id());
        assertThat(disciplinaRepository.buscarDisciplinaPorId(disciplinaA.id(), ESCOLA_B)).isEmpty();
        assertThat(flywayMigrationCount()).isEqualTo(4);
    }

    @Test
    void deveIsolarContratoRestPorEscola() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        Disciplina disciplinaA = disciplinaRepository.salvar(new Disciplina(
                UUID.randomUUID(), ESCOLA_A, "Matematica A", 80, true, now));
        Disciplina disciplinaB = disciplinaRepository.salvar(new Disciplina(
                UUID.randomUUID(), ESCOLA_B, "Matematica B", 60, true, now));

        mockMvc.perform(authenticatedGet("/internal/v1/disciplinas", ESCOLA_A))
                .andExpect(status().isOk())
                .andExpect(header().string(InternalHeaders.CORRELATION_ID, "corr-postgres-51d"))
                .andExpect(jsonPath("$[0].id").value(disciplinaA.id().toString()))
                .andExpect(jsonPath("$[0].escolaId").value(ESCOLA_A.value().toString()))
                .andExpect(jsonPath("$[1]").doesNotExist());

        mockMvc.perform(authenticatedGet("/internal/v1/disciplinas/" + disciplinaA.id(), ESCOLA_B))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        assertThat(disciplinaRepository.buscarDisciplinaPorId(disciplinaB.id(), ESCOLA_B)).isPresent();
    }

    @Test
    void devePersistirEstruturaCompletaDentroDaMesmaEscola() {
        Fixture fixture = createFixture(ESCOLA_A, "A");
        Disciplina disciplina = disciplinaRepository.salvar(new Disciplina(
                UUID.randomUUID(), ESCOLA_A, "Ciencias", 60, true, LocalDateTime.now()));
        TurmaDisciplina vinculo = turmaDisciplinaRepository.salvar(new TurmaDisciplina(
                UUID.randomUUID(), ESCOLA_A, fixture.turma().id(), disciplina.id(),
                60, LocalDateTime.now()));

        assertThat(turmaRepository.buscarTurmaPorId(fixture.turma().id(), ESCOLA_A)).isPresent();
        assertThat(turmaRepository.buscarTurmaPorId(fixture.turma().id(), ESCOLA_B)).isEmpty();
        assertThat(turmaDisciplinaRepository.listarVinculosPorTurma(fixture.turma().id(), ESCOLA_A))
                .extracting(TurmaDisciplina::id).containsExactly(vinculo.id());
    }

    @Test
    void deveExporTodasAsConsultasInternasDoCatalogo() throws Exception {
        Fixture fixture = createFixture(ESCOLA_A, "REST");
        Disciplina disciplina = disciplinaRepository.salvar(new Disciplina(
                UUID.randomUUID(), ESCOLA_A, "Ciencias REST", 60, true, LocalDateTime.now()));
        turmaDisciplinaRepository.salvar(new TurmaDisciplina(
                UUID.randomUUID(), ESCOLA_A, fixture.turma().id(), disciplina.id(),
                60, LocalDateTime.now()));

        mockMvc.perform(authenticatedGet("/internal/v1/catalogos/niveis-ensino", ESCOLA_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").isNotEmpty());
        mockMvc.perform(authenticatedGet("/internal/v1/turnos/" + TURNO_ID, ESCOLA_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("MANHA"));
        mockMvc.perform(authenticatedGet(
                        "/internal/v1/periodos-letivos/" + fixture.periodo().id(), ESCOLA_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escolaId").value(ESCOLA_A.value().toString()));
        mockMvc.perform(authenticatedGet("/internal/v1/series/" + fixture.serie().id(), ESCOLA_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelEnsinoCodigo").value("ENSINO_FUNDAMENTAL"));
        mockMvc.perform(authenticatedGet("/internal/v1/turmas/" + fixture.turma().id(), ESCOLA_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serieNome").value(fixture.serie().nome()))
                .andExpect(jsonPath("$.turnoCodigo").value("MANHA"));
        mockMvc.perform(authenticatedGet("/internal/v1/disciplinas/" + disciplina.id(), ESCOLA_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ciencias REST"));
        mockMvc.perform(authenticatedGet(
                        "/internal/v1/turmas/" + fixture.turma().id() + "/disciplinas", ESCOLA_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].disciplinaNome").value("Ciencias REST"));
    }

    @Test
    void deveBloquearRelacionamentosEntreEscolasNoBanco() {
        Fixture fixtureA = createFixture(ESCOLA_A, "A");
        Fixture fixtureB = createFixture(ESCOLA_B, "B");
        Disciplina disciplinaB = disciplinaRepository.salvar(new Disciplina(
                UUID.randomUUID(), ESCOLA_B, "Geografia", 40, true, LocalDateTime.now()));

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO turma_disciplina (
                    id_turma_disciplina, id_escola, id_turma, id_disciplina, carga_horaria, created_at)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                UUID.randomUUID(), ESCOLA_A.value(), fixtureA.turma().id(), disciplinaB.id(), 40))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO turma (
                    id_turma, id_escola, codigo, nome, capacidade,
                    id_periodo_letivo, id_serie, id_turno, ativo, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, true, CURRENT_TIMESTAMP)
                """,
                UUID.randomUUID(), ESCOLA_A.value(), "CROSS", "Turma cruzada", 20,
                fixtureA.periodo().id(), fixtureB.serie().id(), TURNO_ID))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void devePersistirComandosIdempotenciaEOutboxNaMesmaEstrutura() throws Exception {
        JsonNode periodo = response(authenticatedPost(
                "/internal/v1/periodos-letivos", ESCOLA_A, "periodo-2026",
                """
                {"nome":"2026","ano":2026,"dataInicio":"2026-02-01","dataFim":"2026-12-15"}
                """).andExpect(status().isCreated()));
        JsonNode serie = response(authenticatedPost(
                "/internal/v1/series", ESCOLA_A, "serie-primeiro-ano",
                """
                {"nome":"1 ano","ordem":1,"nivelEnsinoId":"00000000-0000-0000-0000-000000000202"}
                """).andExpect(status().isCreated()));
        String disciplinaBody = """
                {"nome":"Matematica comandos","cargaHoraria":80}
                """;
        JsonNode disciplina = response(authenticatedPost(
                "/internal/v1/disciplinas", ESCOLA_A, "disciplina-matematica", disciplinaBody)
                .andExpect(status().isCreated())
                .andExpect(header().string(InternalHeaders.IDEMPOTENCY_REPLAYED, "false")));
        JsonNode turma = response(authenticatedPost(
                "/internal/v1/turmas", ESCOLA_A, "turma-a",
                """
                {"codigo":"A","nome":"Turma A","capacidade":30,
                 "periodoLetivoId":"%s","serieId":"%s",
                 "turnoId":"00000000-0000-0000-0000-000000000211"}
                """.formatted(periodo.get("id").asText(), serie.get("id").asText()))
                .andExpect(status().isCreated()));
        response(authenticatedPost(
                "/internal/v1/turmas/" + turma.get("id").asText() + "/disciplinas",
                ESCOLA_A,
                "vinculo-matematica",
                """
                {"disciplinaId":"%s","cargaHoraria":80}
                """.formatted(disciplina.get("id").asText()))
                .andExpect(status().isCreated()));

        JsonNode replay = response(authenticatedPost(
                "/internal/v1/disciplinas", ESCOLA_A, "disciplina-matematica", disciplinaBody)
                .andExpect(status().isCreated())
                .andExpect(header().string(InternalHeaders.IDEMPOTENCY_REPLAYED, "true")));

        assertThat(replay.get("id").asText()).isEqualTo(disciplina.get("id").asText());
        assertThat(count("outbox_event")).isEqualTo(5);
        assertThat(count("command_idempotency")).isEqualTo(5);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT status FROM outbox_event WHERE event_type='subject-created'", String.class))
                .isEqualTo("PENDENTE");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT usuario_id FROM outbox_event WHERE event_type='subject-created'", UUID.class))
                .isEqualTo(USUARIO_ID);
    }

    @Test
    void deveRejeitarReusoDaChaveComOutroPayload() throws Exception {
        authenticatedPost(
                "/internal/v1/disciplinas", ESCOLA_A, "same-key",
                "{\"nome\":\"Matematica\",\"cargaHoraria\":80}")
                .andExpect(status().isCreated());

        authenticatedPost(
                "/internal/v1/disciplinas", ESCOLA_A, "same-key",
                "{\"nome\":\"Geografia\",\"cargaHoraria\":40}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_CONFLICT"));

        assertThat(count("disciplina")).isEqualTo(1);
        assertThat(count("outbox_event")).isEqualTo(1);
        assertThat(count("command_idempotency")).isEqualTo(1);
    }

    @Test
    void deveReverterIdempotenciaEOutboxQuandoAgregadoFalha() throws Exception {
        String body = "{\"nome\":\"Duplicada\",\"cargaHoraria\":40}";
        authenticatedPost("/internal/v1/disciplinas", ESCOLA_A, "duplicate-1", body)
                .andExpect(status().isCreated());
        authenticatedPost("/internal/v1/disciplinas", ESCOLA_A, "duplicate-2", body)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CATALOG_CONFLICT"));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM command_idempotency WHERE idempotency_key='duplicate-2'", Integer.class))
                .isZero();
        assertThat(count("outbox_event")).isEqualTo(1);
        assertThat(count("disciplina")).isEqualTo(1);
    }

    @Test
    void deveRejeitarReferenciaDeOutraEscolaSemRegistrarComando() throws Exception {
        Fixture fixtureA = createFixture(ESCOLA_A, "COMMAND-CROSS");

        authenticatedPost(
                "/internal/v1/turmas", ESCOLA_B, "cross-school-command",
                """
                {"codigo":"X","nome":"Turma cruzada","capacidade":20,
                 "periodoLetivoId":"%s","serieId":"%s",
                 "turnoId":"00000000-0000-0000-0000-000000000211"}
                """.formatted(fixtureA.periodo().id(), fixtureA.serie().id()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        assertThat(count("command_idempotency")).isZero();
        assertThat(count("outbox_event")).isZero();
        assertThat(count("turma")).isEqualTo(1);
    }

    private Fixture createFixture(EscolaId escolaId, String suffix) {
        LocalDateTime now = LocalDateTime.now();
        PeriodoLetivo periodo = periodoRepository.salvar(new PeriodoLetivo(
                UUID.randomUUID(), escolaId, "2026-" + suffix, 2026,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 12, 15), true, now));
        Serie serie = serieRepository.salvar(new Serie(
                UUID.randomUUID(), escolaId, "1 ano " + suffix, 1, NIVEL_ENSINO_ID, now));
        Turma turma = turmaRepository.salvar(new Turma(
                UUID.randomUUID(), escolaId, "T-" + suffix, "Turma " + suffix, 30,
                periodo.id(), serie.id(), TURNO_ID, true, now));
        return new Fixture(periodo, serie, turma);
    }

    private int flywayMigrationCount() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success", Integer.class);
        return count == null ? 0 : count;
    }

    private int count(String table) {
        if (!java.util.Set.of(
                "disciplina", "turma", "outbox_event", "command_idempotency").contains(table)) {
            throw new IllegalArgumentException("Tabela de teste nao permitida: " + table);
        }
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM " + table, Integer.class);
        return count == null ? 0 : count;
    }

    private ResultActions authenticatedPost(String path, EscolaId escolaId, String key, String body)
            throws Exception {
        return mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header(InternalHeaders.INTERNAL_TOKEN, "test-internal-token")
                .header(InternalHeaders.CORRELATION_ID, "corr-command-51d")
                .header(InternalHeaders.USUARIO_ID, USUARIO_ID)
                .header(InternalHeaders.ESCOLA_ID, escolaId.value())
                .header(InternalHeaders.IDEMPOTENCY_KEY, key));
    }

    private JsonNode response(ResultActions actions) throws Exception {
        return objectMapper.readTree(actions.andReturn().getResponse().getContentAsByteArray());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authenticatedGet(
            String path,
            EscolaId escolaId) {
        return get(path)
                .header(InternalHeaders.INTERNAL_TOKEN, "test-internal-token")
                .header(InternalHeaders.CORRELATION_ID, "corr-postgres-51d")
                .header(InternalHeaders.USUARIO_ID, USUARIO_ID)
                .header(InternalHeaders.ESCOLA_ID, escolaId.value());
    }

    private record Fixture(PeriodoLetivo periodo, Serie serie, Turma turma) {
    }
}

