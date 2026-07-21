package br.com.escola.enrollmentdocumentservice.infra.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class MatriculaWriteSchemaTest {

    @Test
    void devePrepararCicloDeVidaDeEscritaDaMatricula() throws Exception {
        String url = "jdbc:h2:mem:enrollment-write-schema;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Flyway.configure().dataSource(url, "sa", "").load().migrate();

        try (var connection = DriverManager.getConnection(url, "sa", "");
                var result = connection.getMetaData().getColumns(null, null, "ENROLLMENT_RECORD", "%")) {
            java.util.Set<String> columns = new java.util.HashSet<>();
            while (result.next()) {
                columns.add(result.getString("COLUMN_NAME").toLowerCase());
            }
            assertThat(columns).contains("updated_at", "cancelled_at", "cancellation_reason");
        }
    }
}
