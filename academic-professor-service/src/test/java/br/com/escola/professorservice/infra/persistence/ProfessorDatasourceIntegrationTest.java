package br.com.escola.professorservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class ProfessorDatasourceIntegrationTest {

    private static final String SOURCE_URL = "jdbc:h2:mem:academic-professor-cutover-source;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    private static final String TARGET_URL = "jdbc:h2:mem:academic-professor-cutover-target;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    private static final UUID PROFESSOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000901");

    @Autowired
    private JdbcTemplate target;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        prepararOrigem();
        registry.add("spring.datasource.url", () -> TARGET_URL);
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("professor.persistence.backfill.enabled", () -> true);
        registry.add("professor.persistence.backfill.source-url", () -> SOURCE_URL);
        registry.add("professor.persistence.backfill.source-username", () -> "sa");
        registry.add("professor.persistence.backfill.source-password", () -> "");
    }

    @Test
    void deveAplicarFlywayECarregarAgregadoNoDatasourceProprio() {
        assertThat(target.queryForObject("SELECT COUNT(1) FROM professor WHERE id_professor = ?", Integer.class,
                PROFESSOR_ID)).isOne();
        assertThat(target.queryForObject("SELECT nome_completo FROM professor WHERE id_professor = ?", String.class,
                PROFESSOR_ID)).isEqualTo("Professor de Corte");
        assertThat(target.queryForObject("SELECT COUNT(1) FROM professor_turma_disciplina", Integer.class)).isOne();
    }

    private static void prepararOrigem() {
        JdbcTemplate source = new JdbcTemplate(new DriverManagerDataSource(SOURCE_URL, "sa", ""));
        source.execute("DROP ALL OBJECTS");
        source.execute("CREATE TABLE escola (id_escola UUID PRIMARY KEY, nome VARCHAR(150) NOT NULL)");
        source.execute("CREATE TABLE pessoa (id_pessoa UUID PRIMARY KEY, id_escola UUID NOT NULL, nome_completo VARCHAR(150) NOT NULL)");
        source.execute("""
                CREATE TABLE professor (
                    id_professor UUID PRIMARY KEY, id_pessoa UUID NOT NULL, registro_profissional VARCHAR(80),
                    formacao VARCHAR(150), ativo BOOLEAN NOT NULL, created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP,
                    id_usuario UUID
                )
                """);
        source.execute("CREATE TABLE turma (id_turma UUID PRIMARY KEY, nome VARCHAR(120) NOT NULL)");
        source.execute("CREATE TABLE disciplina (id_disciplina UUID PRIMARY KEY, nome VARCHAR(120) NOT NULL)");
        source.execute("CREATE TABLE turma_disciplina (id_turma_disciplina UUID PRIMARY KEY, id_turma UUID NOT NULL, id_disciplina UUID NOT NULL)");
        source.execute("""
                CREATE TABLE professor_turma_disciplina (
                    id_professor_turma_disciplina UUID PRIMARY KEY, id_professor UUID NOT NULL,
                    id_turma_disciplina UUID NOT NULL, data_inicio DATE, data_fim DATE, ativo BOOLEAN NOT NULL,
                    created_at TIMESTAMP NOT NULL
                )
                """);

        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID pessoaId = UUID.fromString("00000000-0000-0000-0000-000000000902");
        UUID turmaId = UUID.fromString("00000000-0000-0000-0000-000000000903");
        UUID disciplinaId = UUID.fromString("00000000-0000-0000-0000-000000000904");
        UUID turmaDisciplinaId = UUID.fromString("00000000-0000-0000-0000-000000000905");
        LocalDateTime now = LocalDateTime.of(2026, 7, 22, 11, 0);
        source.update("INSERT INTO escola (id_escola, nome) VALUES (?, ?)", escolaId, "Escola corte");
        source.update("INSERT INTO pessoa (id_pessoa, id_escola, nome_completo) VALUES (?, ?, ?)", pessoaId, escolaId,
                "Professor de Corte");
        source.update("""
                INSERT INTO professor (id_professor, id_pessoa, registro_profissional, formacao, ativo, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, PROFESSOR_ID, pessoaId, "RP-901", "Licenciatura", true, now, now);
        source.update("INSERT INTO turma (id_turma, nome) VALUES (?, ?)", turmaId, "Turma de Corte");
        source.update("INSERT INTO disciplina (id_disciplina, nome) VALUES (?, ?)", disciplinaId, "Historia");
        source.update("INSERT INTO turma_disciplina (id_turma_disciplina, id_turma, id_disciplina) VALUES (?, ?, ?)",
                turmaDisciplinaId, turmaId, disciplinaId);
        source.update("""
                INSERT INTO professor_turma_disciplina (
                    id_professor_turma_disciplina, id_professor, id_turma_disciplina, ativo, created_at
                ) VALUES (?, ?, ?, ?, ?)
                """, UUID.fromString("00000000-0000-0000-0000-000000000906"), PROFESSOR_ID,
                turmaDisciplinaId, true, now);
    }
}
