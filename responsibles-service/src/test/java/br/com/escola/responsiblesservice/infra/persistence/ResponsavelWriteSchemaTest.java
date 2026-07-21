package br.com.escola.responsiblesservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class ResponsavelWriteSchemaTest {

    @Test
    void devePrepararCicloDeVidaEUnicidadeDoResponsavelPorEscola() throws Exception {
        String url = "jdbc:h2:mem:responsibles-write-schema;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/responsibles/migration")
                .load()
                .migrate();

        UUID escolaId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO responsavel (
                        id_responsavel, nome_completo, cpf, id_escola, created_at
                    ) VALUES (
                        '%s', 'Responsavel Ativo', '12345678901', '%s', TIMESTAMP '%s'
                    )
                    """.formatted(responsavelId, escolaId, "2026-07-21 14:00:00"));

            try (var result = statement.executeQuery("""
                    SELECT ativo, updated_at
                    FROM responsavel
                    WHERE id_responsavel = '%s'
                    """.formatted(responsavelId))) {
                assertThat(result.next()).isTrue();
                assertThat(result.getBoolean("ativo")).isTrue();
                assertThat(result.getObject("updated_at", LocalDateTime.class)).isNull();
            }

            assertThatThrownBy(() -> statement.executeUpdate("""
                    INSERT INTO responsavel (
                        id_responsavel, nome_completo, cpf, id_escola, created_at
                    ) VALUES (
                        '%s', 'Responsavel Duplicado', '12345678901', '%s', TIMESTAMP '%s'
                    )
                    """.formatted(UUID.randomUUID(), escolaId, "2026-07-21 14:01:00")))
                    .isInstanceOf(java.sql.SQLException.class);
        }
    }
}
