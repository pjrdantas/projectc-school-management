package br.com.escola.professorservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class ProfessorBackfillExecutorTest {

    @Test
    void deveImportarReconciliarERepetirSemApagarProfessorLocalPosterior() {
        JdbcTemplate source = sourceDatabase();
        JdbcTemplate target = targetDatabase();
        UUID escolaId = UUID.randomUUID();
        UUID professorId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID disciplinaId = UUID.randomUUID();
        UUID turmaDisciplinaId = UUID.randomUUID();
        UUID alocacaoId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 7, 22, 10, 30);

        source.update("INSERT INTO escola (id_escola, nome) VALUES (?, ?)", escolaId, "Escola origem");
        source.update("""
                INSERT INTO pessoa (id_pessoa, id_escola, nome_completo) VALUES (?, ?, ?)
                """, pessoaId, escolaId, "Ana Importada");
        source.update("""
                INSERT INTO professor (id_professor, id_pessoa, registro_profissional, formacao, ativo, created_at, updated_at, id_usuario)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, professorId, pessoaId, "RP-100", "Licenciatura", true, now, now, UUID.randomUUID());
        source.update("INSERT INTO turma (id_turma, nome) VALUES (?, ?)", turmaId, "Turma A");
        source.update("INSERT INTO disciplina (id_disciplina, nome) VALUES (?, ?)", disciplinaId, "Matematica");
        source.update("INSERT INTO turma_disciplina (id_turma_disciplina, id_turma, id_disciplina) VALUES (?, ?, ?)",
                turmaDisciplinaId, turmaId, disciplinaId);
        source.update("""
                INSERT INTO professor_turma_disciplina (
                    id_professor_turma_disciplina, id_professor, id_turma_disciplina, data_inicio, data_fim, ativo, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """, alocacaoId, professorId, turmaDisciplinaId, LocalDate.of(2026, 2, 1), null, true, now);

        ProfessorBackfillExecutor executor = new ProfessorBackfillExecutor(source, target, 1);
        ProfessorBackfillReport first = executor.execute();
        UUID localProfessorId = UUID.randomUUID();
        target.update("""
                INSERT INTO professor (
                    id_professor, id_pessoa, nome_completo, id_escola, escola_nome, ativo, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """, localProfessorId, UUID.randomUUID(), "Professor Local Posterior", escolaId, "Escola origem", true, now);
        ProfessorBackfillReport second = executor.execute();

        assertThat(first.reconciled()).isTrue();
        assertThat(second.reconciled()).isTrue();
        assertThat(first.copiedRows()).containsEntry("professor", 1).containsEntry("professor_turma_disciplina", 1);
        assertThat(second.sourceRows()).isEqualTo(second.reconciledRows());
        assertThat(target.queryForObject("SELECT nome_completo FROM professor WHERE id_professor = ?", String.class, professorId))
                .isEqualTo("Ana Importada");
        assertThat(target.queryForObject("SELECT nome_turma FROM professor_turma_disciplina WHERE id_professor_turma_disciplina = ?",
                String.class, alocacaoId)).isEqualTo("Turma A");
        assertThat(target.queryForObject("SELECT COUNT(1) FROM professor", Integer.class)).isEqualTo(2);
    }

    private JdbcTemplate sourceDatabase() {
        JdbcTemplate jdbc = jdbc("jdbc:h2:mem:academic-professor-backfill-source;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        jdbc.execute("CREATE TABLE escola (id_escola UUID PRIMARY KEY, nome VARCHAR(150) NOT NULL)");
        jdbc.execute("CREATE TABLE pessoa (id_pessoa UUID PRIMARY KEY, id_escola UUID NOT NULL, nome_completo VARCHAR(150) NOT NULL)");
        jdbc.execute("""
                CREATE TABLE professor (
                    id_professor UUID PRIMARY KEY, id_pessoa UUID NOT NULL, registro_profissional VARCHAR(80),
                    formacao VARCHAR(150), ativo BOOLEAN NOT NULL, created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP,
                    id_usuario UUID
                )
                """);
        jdbc.execute("CREATE TABLE turma (id_turma UUID PRIMARY KEY, nome VARCHAR(120) NOT NULL)");
        jdbc.execute("CREATE TABLE disciplina (id_disciplina UUID PRIMARY KEY, nome VARCHAR(120) NOT NULL)");
        jdbc.execute("""
                CREATE TABLE turma_disciplina (
                    id_turma_disciplina UUID PRIMARY KEY, id_turma UUID NOT NULL, id_disciplina UUID NOT NULL
                )
                """);
        jdbc.execute("""
                CREATE TABLE professor_turma_disciplina (
                    id_professor_turma_disciplina UUID PRIMARY KEY, id_professor UUID NOT NULL,
                    id_turma_disciplina UUID NOT NULL, data_inicio DATE, data_fim DATE, ativo BOOLEAN NOT NULL,
                    created_at TIMESTAMP NOT NULL
                )
                """);
        return jdbc;
    }

    private JdbcTemplate targetDatabase() {
        String url = "jdbc:h2:mem:academic-professor-backfill-target;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration").load().migrate();
        return jdbc(url);
    }

    private JdbcTemplate jdbc(String url) {
        return new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
    }
}
