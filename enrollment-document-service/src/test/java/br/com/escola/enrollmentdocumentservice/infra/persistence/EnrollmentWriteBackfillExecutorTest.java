package br.com.escola.enrollmentdocumentservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class EnrollmentWriteBackfillExecutorTest {

    @Test
    void deveCarregarReconciliarERepetirSemApagarMatriculaLocal() {
        JdbcTemplate source = sourceDatabase();
        JdbcTemplate target = targetDatabase();
        UUID matriculaId = UUID.fromString("00000000-0000-0000-0000-000000000901");
        UUID etapaId = UUID.fromString("00000000-0000-0000-0000-000000000902");
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID alunoId = UUID.fromString("00000000-0000-0000-0000-000000000903");
        UUID turmaId = UUID.fromString("00000000-0000-0000-0000-000000000071");
        UUID serieId = UUID.fromString("00000000-0000-0000-0000-000000000081");
        UUID periodoId = UUID.fromString("00000000-0000-0000-0000-000000000091");
        LocalDateTime now = LocalDateTime.of(2026, 7, 21, 18, 30);
        prepararOrigem(source, matriculaId, etapaId, escolaId, alunoId, turmaId, serieId, periodoId, now);

        EnrollmentWriteBackfillExecutor executor = new EnrollmentWriteBackfillExecutor(source, target, 1);
        EnrollmentWriteBackfillReport first = executor.execute();
        target.update("""
                INSERT INTO enrollment_record (
                    id, school_id, student_id, class_id, grade_id, term_id, status, enrollment_type, enrollment_date, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID(), escolaId, UUID.randomUUID(), turmaId, serieId, periodoId,
                "PENDENTE", "PRIMEIRA_MATRICULA", LocalDate.of(2026, 7, 21), now);
        EnrollmentWriteBackfillReport second = executor.execute();

        assertThat(first.reconciled()).isTrue();
        assertThat(second.reconciled()).isTrue();
        assertThat(target.queryForObject("SELECT COUNT(1) FROM enrollment_record", Integer.class)).isEqualTo(2);
        assertThat(target.queryForObject("SELECT status FROM enrollment_record WHERE id = ?", String.class, matriculaId))
                .isEqualTo("EFETIVADA");
        assertThat(target.queryForObject("SELECT COUNT(1) FROM enrollment_step WHERE id = ?", Integer.class, etapaId)).isOne();
    }

    private JdbcTemplate sourceDatabase() {
        JdbcTemplate source = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:enrollment-backfill-source;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", ""));
        source.execute("DROP ALL OBJECTS");
        source.execute("CREATE TABLE escola (id_escola UUID PRIMARY KEY, nome VARCHAR(150))");
        source.execute("CREATE TABLE pessoa (id_pessoa UUID PRIMARY KEY, id_escola UUID)");
        source.execute("CREATE TABLE aluno (id_aluno UUID PRIMARY KEY, id_pessoa UUID)");
        source.execute("CREATE TABLE serie (id_serie UUID PRIMARY KEY, nome VARCHAR(120))");
        source.execute("CREATE TABLE turma (id_turma UUID PRIMARY KEY, id_serie UUID)");
        source.execute("CREATE TABLE status_matricula (id_status_matricula UUID PRIMARY KEY, codigo VARCHAR(80))");
        source.execute("CREATE TABLE tipo_matricula (id_tipo_matricula UUID PRIMARY KEY, codigo VARCHAR(80))");
        source.execute("""
                CREATE TABLE matricula (
                    id_matricula UUID PRIMARY KEY, id_aluno UUID, id_turma UUID, id_periodo_letivo UUID,
                    id_status_matricula UUID, id_tipo_matricula UUID, data_solicitacao DATE, observacao VARCHAR(500), created_at TIMESTAMP
                )
                """);
        source.execute("CREATE TABLE status_etapa_matricula (id_status_etapa_matricula UUID PRIMARY KEY, codigo VARCHAR(80))");
        source.execute("""
                CREATE TABLE matricula_etapa (
                    id_matricula_etapa UUID PRIMARY KEY, id_matricula UUID, descricao VARCHAR(180), ordem INTEGER,
                    id_status_etapa_matricula UUID, data_inicio TIMESTAMP, data_conclusao TIMESTAMP, observacao VARCHAR(500)
                )
                """);
        return source;
    }

    private JdbcTemplate targetDatabase() {
        String url = "jdbc:h2:mem:enrollment-backfill-target;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration").load().migrate();
        return new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
    }

    private void prepararOrigem(
            JdbcTemplate source,
            UUID matriculaId,
            UUID etapaId,
            UUID escolaId,
            UUID alunoId,
            UUID turmaId,
            UUID serieId,
            UUID periodoId,
            LocalDateTime now) {
        UUID pessoaId = UUID.randomUUID();
        UUID statusId = UUID.randomUUID();
        UUID tipoId = UUID.randomUUID();
        UUID statusEtapaId = UUID.randomUUID();
        source.update("INSERT INTO escola VALUES (?, ?)", escolaId, "Escola de corte");
        source.update("INSERT INTO pessoa VALUES (?, ?)", pessoaId, escolaId);
        source.update("INSERT INTO aluno VALUES (?, ?)", alunoId, pessoaId);
        source.update("INSERT INTO serie VALUES (?, ?)", serieId, "6 Ano");
        source.update("INSERT INTO turma VALUES (?, ?)", turmaId, serieId);
        source.update("INSERT INTO status_matricula VALUES (?, ?)", statusId, "EFETIVADA");
        source.update("INSERT INTO tipo_matricula VALUES (?, ?)", tipoId, "PRIMEIRA_MATRICULA");
        source.update("INSERT INTO status_etapa_matricula VALUES (?, ?)", statusEtapaId, "CONCLUIDA");
        source.update("""
                INSERT INTO matricula VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, matriculaId, alunoId, turmaId, periodoId, statusId, tipoId, LocalDate.of(2026, 7, 21), "Migrada", now);
        source.update("""
                INSERT INTO matricula_etapa VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, etapaId, matriculaId, "Documentos", 1, statusEtapaId, now, now, "Validado");
    }
}
