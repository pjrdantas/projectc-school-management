package br.com.escola.catalog.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import br.com.escola.catalog.domain.model.Disciplina;
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

@Testcontainers
@SpringBootTest(properties = "management.health.redis.enabled=false")
class CatalogPersistenceIT {

    private static final UUID NIVEL_ENSINO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000202");
    private static final UUID TURNO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000211");
    private static final EscolaId ESCOLA_A = new EscolaId(
            UUID.fromString("00000000-0000-0000-0000-0000000000a1"));
    private static final EscolaId ESCOLA_B = new EscolaId(
            UUID.fromString("00000000-0000-0000-0000-0000000000b1"));

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

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM turma_disciplina");
        jdbcTemplate.update("DELETE FROM turma");
        jdbcTemplate.update("DELETE FROM disciplina");
        jdbcTemplate.update("DELETE FROM serie");
        jdbcTemplate.update("DELETE FROM periodo_letivo");
        jdbcTemplate.update("DELETE FROM outbox_event");
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
        assertThat(flywayMigrationCount()).isEqualTo(2);
    }

    @Test
    void devePersistirEstruturaCompletaDentroDaMesmaEscola() {
        CatalogFixture fixture = createFixture(ESCOLA_A, "A");
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
    void deveBloquearRelacionamentosEntreEscolasNoBanco() {
        CatalogFixture fixtureA = createFixture(ESCOLA_A, "A");
        CatalogFixture fixtureB = createFixture(ESCOLA_B, "B");
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

    private CatalogFixture createFixture(EscolaId escolaId, String suffix) {
        LocalDateTime now = LocalDateTime.now();
        PeriodoLetivo periodo = periodoRepository.salvar(new PeriodoLetivo(
                UUID.randomUUID(), escolaId, "2026-" + suffix, 2026,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 12, 15), true, now));
        Serie serie = serieRepository.salvar(new Serie(
                UUID.randomUUID(), escolaId, "1 ano " + suffix, 1, NIVEL_ENSINO_ID, now));
        Turma turma = turmaRepository.salvar(new Turma(
                UUID.randomUUID(), escolaId, "T-" + suffix, "Turma " + suffix, 30,
                periodo.id(), serie.id(), TURNO_ID, true, now));
        return new CatalogFixture(periodo, serie, turma);
    }

    private int flywayMigrationCount() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success", Integer.class);
        return count == null ? 0 : count;
    }

    private record CatalogFixture(PeriodoLetivo periodo, Serie serie, Turma turma) {
    }
}
