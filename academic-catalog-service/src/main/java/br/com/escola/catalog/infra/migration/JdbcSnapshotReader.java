package br.com.escola.catalog.infra.migration;

import java.util.Objects;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;

import br.com.escola.catalog.application.migration.MigracaoSnapshot;

final class JdbcSnapshotReader {

    private JdbcSnapshotReader() {}

    static MigracaoSnapshot read(JdbcTemplate jdbc, boolean source) {
        var niveis = jdbc.query("""
                SELECT id_nivel_ensino, codigo, descricao
                  FROM nivel_ensino ORDER BY id_nivel_ensino
                """, (rs, row) -> new MigracaoSnapshot.NivelEnsinoRow(
                rs.getObject("id_nivel_ensino", UUID.class), rs.getString("codigo"), rs.getString("descricao")));
        var turnos = jdbc.query("""
                SELECT id_turno, codigo, descricao FROM turno ORDER BY id_turno
                """, (rs, row) -> new MigracaoSnapshot.TurnoRow(
                rs.getObject("id_turno", UUID.class), rs.getString("codigo"), rs.getString("descricao")));
        var periodos = jdbc.query("""
                SELECT id_periodo_letivo, id_escola, nome, ano, data_inicio, data_fim, ativo, created_at
                  FROM periodo_letivo ORDER BY id_escola, id_periodo_letivo
                """, (rs, row) -> new MigracaoSnapshot.PeriodoLetivoRow(
                rs.getObject("id_periodo_letivo", UUID.class), rs.getObject("id_escola", UUID.class),
                rs.getString("nome"), rs.getInt("ano"), rs.getObject("data_inicio", java.time.LocalDate.class),
                rs.getObject("data_fim", java.time.LocalDate.class), rs.getBoolean("ativo"),
                rs.getObject("created_at", java.time.LocalDateTime.class)));
        var series = jdbc.query("""
                SELECT id_serie, id_escola, nome, ordem, id_nivel_ensino, created_at
                  FROM serie ORDER BY id_escola, id_serie
                """, (rs, row) -> new MigracaoSnapshot.SerieRow(
                rs.getObject("id_serie", UUID.class), rs.getObject("id_escola", UUID.class),
                rs.getString("nome"), rs.getInt("ordem"), rs.getObject("id_nivel_ensino", UUID.class),
                rs.getObject("created_at", java.time.LocalDateTime.class)));
        var disciplinas = jdbc.query("""
                SELECT id_disciplina, id_escola, nome, carga_horaria, ativo, created_at
                  FROM disciplina ORDER BY id_escola, id_disciplina
                """, (rs, row) -> new MigracaoSnapshot.DisciplinaRow(
                rs.getObject("id_disciplina", UUID.class), rs.getObject("id_escola", UUID.class),
                rs.getString("nome"), (Integer) rs.getObject("carga_horaria"), rs.getBoolean("ativo"),
                rs.getObject("created_at", java.time.LocalDateTime.class)));
        var turmas = jdbc.query("""
                SELECT id_turma, id_escola, codigo, nome, capacidade, id_periodo_letivo,
                       id_serie, id_turno, ativo, created_at
                  FROM turma ORDER BY id_escola, id_turma
                """, (rs, row) -> new MigracaoSnapshot.TurmaRow(
                rs.getObject("id_turma", UUID.class), rs.getObject("id_escola", UUID.class),
                rs.getString("codigo"), rs.getString("nome"), rs.getInt("capacidade"),
                rs.getObject("id_periodo_letivo", UUID.class), rs.getObject("id_serie", UUID.class),
                rs.getObject("id_turno", UUID.class), rs.getBoolean("ativo"),
                rs.getObject("created_at", java.time.LocalDateTime.class)));
        var vinculos = source ? readSourceLinks(jdbc) : readTargetLinks(jdbc);
        return new MigracaoSnapshot(niveis, turnos, periodos, series, disciplinas, turmas, vinculos);
    }

    private static java.util.List<MigracaoSnapshot.TurmaDisciplinaRow> readSourceLinks(JdbcTemplate jdbc) {
        return jdbc.query("""
                SELECT td.id_turma_disciplina, t.id_escola AS turma_escola,
                       d.id_escola AS disciplina_escola, td.id_turma, td.id_disciplina,
                       td.carga_horaria, td.created_at
                  FROM turma_disciplina td
                  JOIN turma t ON t.id_turma = td.id_turma
                  JOIN disciplina d ON d.id_disciplina = td.id_disciplina
                 ORDER BY t.id_escola, td.id_turma_disciplina
                """, (rs, row) -> {
            UUID turmaEscola = rs.getObject("turma_escola", UUID.class);
            UUID disciplinaEscola = rs.getObject("disciplina_escola", UUID.class);
            if (!Objects.equals(turmaEscola, disciplinaEscola)) {
                throw new IllegalStateException(
                        "Vinculo turma-disciplina cruza escolas: " + rs.getObject("id_turma_disciplina"));
            }
            return new MigracaoSnapshot.TurmaDisciplinaRow(
                    rs.getObject("id_turma_disciplina", UUID.class), turmaEscola,
                    rs.getObject("id_turma", UUID.class), rs.getObject("id_disciplina", UUID.class),
                    (Integer) rs.getObject("carga_horaria"),
                    rs.getObject("created_at", java.time.LocalDateTime.class));
        });
    }

    private static java.util.List<MigracaoSnapshot.TurmaDisciplinaRow> readTargetLinks(JdbcTemplate jdbc) {
        return jdbc.query("""
                SELECT id_turma_disciplina, id_escola, id_turma, id_disciplina, carga_horaria, created_at
                  FROM turma_disciplina ORDER BY id_escola, id_turma_disciplina
                """, (rs, row) -> new MigracaoSnapshot.TurmaDisciplinaRow(
                rs.getObject("id_turma_disciplina", UUID.class), rs.getObject("id_escola", UUID.class),
                rs.getObject("id_turma", UUID.class), rs.getObject("id_disciplina", UUID.class),
                (Integer) rs.getObject("carga_horaria"),
                rs.getObject("created_at", java.time.LocalDateTime.class)));
    }
}

