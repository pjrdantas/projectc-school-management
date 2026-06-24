package br.com.escola.support;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Profile("professor-shadow-operational")
@Slf4j
public class ProfessorShadowOperationalSmokeDataLoader implements ApplicationRunner {

    public static final UUID ESCOLA_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");
    public static final UUID USUARIO_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    public static final UUID PESSOA_ID = UUID.fromString("30000000-0000-0000-0000-000000000002");
    public static final UUID CARGO_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");
    public static final UUID FUNCIONARIO_ID = UUID.fromString("30000000-0000-0000-0000-000000000004");
    public static final UUID PROFESSOR_ID = UUID.fromString("30000000-0000-0000-0000-000000000005");
    public static final UUID PESSOA_ELEGIVEL_ID = UUID.fromString("30000000-0000-0000-0000-000000000006");
    public static final UUID CARGO_ELEGIVEL_ID = UUID.fromString("30000000-0000-0000-0000-000000000007");
    public static final UUID FUNCIONARIO_ELEGIVEL_ID = UUID.fromString("30000000-0000-0000-0000-000000000008");
    public static final UUID PERIODO_ID = UUID.fromString("30000000-0000-0000-0000-000000000009");
    public static final UUID TURMA_ID = UUID.fromString("30000000-0000-0000-0000-000000000010");
    public static final UUID DISCIPLINA_ID = UUID.fromString("30000000-0000-0000-0000-000000000011");
    public static final UUID TURMA_DISCIPLINA_ID = UUID.fromString("30000000-0000-0000-0000-000000000012");
    public static final UUID PROFESSOR_TURMA_DISCIPLINA_ID = UUID.fromString("30000000-0000-0000-0000-000000000013");
    public static final UUID NIVEL_ENSINO_ID = UUID.fromString("00000000-0000-0000-0000-000000000042");
    public static final UUID SERIE_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");
    public static final UUID TURNO_MANHA_ID = UUID.fromString("00000000-0000-0000-0000-000000000051");
    public static final String USERNAME = "professor-shadow-smoke";
    public static final String PASSWORD = "senha123";

    private final JdbcTemplate jdbcTemplate;

    public ProfessorShadowOperationalSmokeDataLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Inicializando seed do smoke operacional de professor para usuario {}", USERNAME);
        inserirUsuarioSeAusente();
        inserirProfessorSeAusente();
        inserirFuncionarioElegivelSeAusente();
        inserirTurmaDisciplinaEAlocacaoSeAusente();
        log.info("Seed do smoke operacional de professor concluido para usuario {} e professor {}", USERNAME, PROFESSOR_ID);
    }

    private void inserirUsuarioSeAusente() {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM usuario WHERE id_usuario = ?",
                Integer.class,
                USUARIO_ID);
        if (total != null && total > 0) {
            return;
        }

        jdbcTemplate.update("""
                INSERT INTO usuario (id_usuario, username, nome, email, senha_hash, ativo, created_at)
                VALUES (?, ?, ?, ?, ?, true, ?)
                """,
                USUARIO_ID,
                USERNAME,
                "Professor Shadow Smoke",
                "professor.shadow.smoke@example.com",
                PASSWORD,
                LocalDateTime.now());
    }

    private void inserirProfessorSeAusente() {
        Integer totalProfessor = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM professor WHERE id_professor = ?",
                Integer.class,
                PROFESSOR_ID);
        if (totalProfessor != null && totalProfessor > 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        if (!registroExiste("SELECT COUNT(1) FROM escola WHERE id_escola = ?", ESCOLA_ID)) {
            jdbcTemplate.update("""
                    INSERT INTO escola (id_escola, nome, ativo, created_at)
                    VALUES (?, ?, true, ?)
                    """,
                    ESCOLA_ID,
                    "Escola padrão",
                    now);
        }

        if (!registroExiste("SELECT COUNT(1) FROM pessoa WHERE id_pessoa = ?", PESSOA_ID)) {
            jdbcTemplate.update("""
                    INSERT INTO pessoa (id_pessoa, nome_completo, cpf, email, id_escola, ativo, created_at)
                    VALUES (?, ?, ?, ?, ?, true, ?)
                    """,
                    PESSOA_ID,
                    "Professor Shadow Smoke",
                    "30000000001",
                    "professor.shadow.smoke@example.com",
                    ESCOLA_ID,
                    now);
        }

        if (!registroExiste("SELECT COUNT(1) FROM cargo WHERE id_cargo = ?", CARGO_ID)) {
            jdbcTemplate.update("""
                    INSERT INTO cargo (id_cargo, codigo, descricao)
                    VALUES (?, ?, ?)
                    """,
                    CARGO_ID,
                    "PROF-SHADOW-SMOKE",
                    "Professor");
        }

        if (!registroExiste("SELECT COUNT(1) FROM funcionario WHERE id_funcionario = ?", FUNCIONARIO_ID)) {
            jdbcTemplate.update("""
                    INSERT INTO funcionario (id_funcionario, id_pessoa, id_cargo, ativo, created_at)
                    VALUES (?, ?, ?, true, ?)
                    """,
                    FUNCIONARIO_ID,
                    PESSOA_ID,
                    CARGO_ID,
                    now);
        }

        jdbcTemplate.update("""
                INSERT INTO professor
                (id_professor, registro_profissional, formacao, ativo, created_at, updated_at, id_pessoa, id_usuario)
                VALUES (?, ?, ?, true, ?, ?, ?, ?)
                """,
                PROFESSOR_ID,
                "RP-SHADOW-SMOKE",
                "Licenciatura em Matemática",
                now,
                now,
                PESSOA_ID,
                USUARIO_ID);
    }

    private void inserirFuncionarioElegivelSeAusente() {
        LocalDateTime now = LocalDateTime.now();

        if (!registroExiste("SELECT COUNT(1) FROM pessoa WHERE id_pessoa = ?", PESSOA_ELEGIVEL_ID)) {
            jdbcTemplate.update("""
                    INSERT INTO pessoa (id_pessoa, nome_completo, cpf, email, id_escola, ativo, created_at)
                    VALUES (?, ?, ?, ?, ?, true, ?)
                    """,
                    PESSOA_ELEGIVEL_ID,
                    "Funcionario Elegivel Shadow Smoke",
                    "30000000002",
                    "funcionario.elegivel.shadow.smoke@example.com",
                    ESCOLA_ID,
                    now);
        }

        if (!registroExiste("SELECT COUNT(1) FROM cargo WHERE id_cargo = ?", CARGO_ELEGIVEL_ID)) {
            jdbcTemplate.update("""
                    INSERT INTO cargo (id_cargo, codigo, descricao)
                    VALUES (?, ?, ?)
                    """,
                    CARGO_ELEGIVEL_ID,
                    "PROF-SHADOW-ELEGIVEL",
                    "Professor");
        }

        if (!registroExiste("SELECT COUNT(1) FROM funcionario WHERE id_funcionario = ?", FUNCIONARIO_ELEGIVEL_ID)) {
            jdbcTemplate.update("""
                    INSERT INTO funcionario (id_funcionario, id_pessoa, id_cargo, ativo, created_at)
                    VALUES (?, ?, ?, true, ?)
                    """,
                    FUNCIONARIO_ELEGIVEL_ID,
                    PESSOA_ELEGIVEL_ID,
                    CARGO_ELEGIVEL_ID,
                    now);
        }
    }

    private void inserirTurmaDisciplinaEAlocacaoSeAusente() {
        LocalDateTime now = LocalDateTime.now();

        if (!registroExiste("SELECT COUNT(1) FROM nivel_ensino WHERE id_nivel_ensino = ?", NIVEL_ENSINO_ID)) {
            jdbcTemplate.update("""
                    INSERT INTO nivel_ensino (id_nivel_ensino, codigo, descricao)
                    VALUES (?, ?, ?)
                    """,
                    NIVEL_ENSINO_ID,
                    "ENSINO_FUNDAMENTAL",
                    "Ensino Fundamental");
        }

        if (!registroExiste("SELECT COUNT(1) FROM turno WHERE id_turno = ?", TURNO_MANHA_ID)) {
            jdbcTemplate.update("""
                    INSERT INTO turno (id_turno, codigo, descricao)
                    VALUES (?, ?, ?)
                    """,
                    TURNO_MANHA_ID,
                    "MANHA",
                    "Manha");
        }

        if (!registroExiste("SELECT COUNT(1) FROM serie WHERE id_serie = ?", SERIE_PADRAO_ID)) {
            jdbcTemplate.update("""
                    INSERT INTO serie
                    (id_serie, id_nivel_ensino, id_escola, nome, ordem, created_at)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    SERIE_PADRAO_ID,
                    NIVEL_ENSINO_ID,
                    ESCOLA_ID,
                    "Serie padrao",
                    1,
                    now);
        }

        if (!registroExiste("SELECT COUNT(1) FROM periodo_letivo WHERE id_periodo_letivo = ?", PERIODO_ID)) {
            jdbcTemplate.update("""
                    INSERT INTO periodo_letivo
                    (id_periodo_letivo, nome, ano, data_inicio, data_fim, ativo, id_escola, created_at)
                    VALUES (?, ?, ?, ?, ?, true, ?, ?)
                    """,
                    PERIODO_ID,
                    "PROF-SHADOW-SMOKE-2041.1",
                    2041,
                    LocalDate.parse("2041-02-01"),
                    LocalDate.parse("2041-06-30"),
                    ESCOLA_ID,
                    now);
        }

        if (!registroExiste("SELECT COUNT(1) FROM turma WHERE id_turma = ?", TURMA_ID)) {
            jdbcTemplate.update("""
                    INSERT INTO turma
                    (id_turma, codigo, nome, capacidade, id_periodo_letivo, id_serie, id_turno, ativo, id_escola, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, true, ?, ?)
                    """,
                    TURMA_ID,
                    "PROF-SHADOW-A",
                    "Professor Shadow Smoke Turma A",
                    30,
                    PERIODO_ID,
                    SERIE_PADRAO_ID,
                    TURNO_MANHA_ID,
                    ESCOLA_ID,
                    now);
        }

        if (!registroExiste("SELECT COUNT(1) FROM disciplina WHERE id_disciplina = ?", DISCIPLINA_ID)) {
            jdbcTemplate.update("""
                    INSERT INTO disciplina
                    (id_disciplina, nome, carga_horaria, ativo, id_escola, created_at)
                    VALUES (?, ?, ?, true, ?, ?)
                    """,
                    DISCIPLINA_ID,
                    "Professor Shadow Smoke Matematica",
                    80,
                    ESCOLA_ID,
                    now);
        }

        if (!registroExiste("SELECT COUNT(1) FROM turma_disciplina WHERE id_turma_disciplina = ?", TURMA_DISCIPLINA_ID)) {
            jdbcTemplate.update("""
                    INSERT INTO turma_disciplina
                    (id_turma_disciplina, carga_horaria, created_at, id_disciplina, id_turma)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    TURMA_DISCIPLINA_ID,
                    80,
                    now,
                    DISCIPLINA_ID,
                    TURMA_ID);
        }

        if (!registroExiste("SELECT COUNT(1) FROM professor_turma_disciplina WHERE id_professor_turma_disciplina = ?", PROFESSOR_TURMA_DISCIPLINA_ID)) {
            jdbcTemplate.update("""
                    INSERT INTO professor_turma_disciplina
                    (id_professor_turma_disciplina, data_inicio, data_fim, ativo, created_at, id_professor, id_turma_disciplina)
                    VALUES (?, ?, ?, true, ?, ?, ?)
                    """,
                    PROFESSOR_TURMA_DISCIPLINA_ID,
                    LocalDate.parse("2041-02-01"),
                    null,
                    now,
                    PROFESSOR_ID,
                    TURMA_DISCIPLINA_ID);
        }
    }

    private boolean registroExiste(String sql, UUID id) {
        Integer total = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return total != null && total > 0;
    }
}
