package br.com.escola.pedagogicalservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.fasterxml.jackson.databind.ObjectMapper;

class HistoricoEscolarBackfillExecutorTest {

    @Test
    void preservaDocumentoEItensLegadosSemFabricarVinculos() {
        JdbcTemplate source = jdbc("historico_source");
        JdbcTemplate target = jdbc("historico_target");
        UUID historicoId = UUID.randomUUID();
        UUID schoolId = UUID.randomUUID();
        source.execute("CREATE TABLE historico_escolar (id UUID PRIMARY KEY, nome_aluno VARCHAR(255), ano_conclusao INT)");
        source.execute("CREATE TABLE historico_escolar_item (id UUID PRIMARY KEY, historico_escolar_id UUID, componente_curricular VARCHAR(150))");
        source.update("INSERT INTO historico_escolar (id, nome_aluno, ano_conclusao) VALUES (?, ?, ?)", historicoId, "Ana", 2025);
        source.update("INSERT INTO historico_escolar_item (id, historico_escolar_id, componente_curricular) VALUES (?, ?, ?)",
                UUID.randomUUID(), historicoId, "Matematica");
        target.execute("""
                CREATE TABLE academic_history_record (
                    id UUID PRIMARY KEY, school_id UUID NOT NULL, student_id UUID, enrollment_id UUID,
                    screen_mode VARCHAR(40), screen_payload_json CLOB, write_payload_json CLOB, updated_at TIMESTAMP NOT NULL
                )
                """);

        HistoricoEscolarBackfillReport report = new HistoricoEscolarBackfillExecutor(
                source, target, new ObjectMapper(), schoolId, 1).execute();

        assertThat(report).isEqualTo(new HistoricoEscolarBackfillReport(1, 1, 1, true));
        String payload = target.queryForObject("SELECT screen_payload_json FROM academic_history_record WHERE id = ?", String.class, historicoId);
        assertThat(payload).contains("Ana", "Matematica", "HISTORICO_LEGADO");
        assertThat(target.queryForObject("SELECT student_id FROM academic_history_record WHERE id = ?", Object.class, historicoId)).isNull();
        assertThat(target.queryForObject("SELECT enrollment_id FROM academic_history_record WHERE id = ?", Object.class, historicoId)).isNull();
    }

    private JdbcTemplate jdbc(String database) {
        return new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:" + database + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", ""));
    }
}
