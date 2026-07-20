package br.com.escola.peopleservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class AlunoSchemaTest {

    @Test
    void deveEvoluirAlunoExistenteParaEscritaPropria() throws SQLException {
        String url = "jdbc:h2:mem:people_student_schema_" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/people-readmodel/migration")
                .target("9")
                .load()
                .migrate();

        UUID escolaId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 20, 17, 45);
        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            update(connection, """
                    INSERT INTO pessoa (
                        id_pessoa, id_escola, escola_nome, nome_completo, cpf, ativo, created_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, pessoaId, escolaId, "Escola B3", "Aluno existente",
                    "12345678901", true, createdAt);
            update(connection, """
                    INSERT INTO aluno (
                        id_aluno, id_pessoa, nome_completo, cpf, data_nascimento, created_at
                    ) VALUES (?, ?, ?, ?, ?, ?)
                    """, alunoId, pessoaId, "Aluno existente", "12345678901",
                    LocalDate.of(2015, 3, 10), createdAt);
        }

        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/people-readmodel/migration")
                .load()
                .migrate();

        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            assertThat(query(connection, "id_escola", alunoId)).isEqualTo(escolaId);
            assertThat(query(connection, "ativo", alunoId)).isEqualTo(true);
            assertThat(query(connection, "emancipado", alunoId)).isEqualTo(false);
            assertThatThrownBy(() -> update(
                    connection,
                    "UPDATE aluno SET id_status_aluno = ? WHERE id_aluno = ?",
                    UUID.randomUUID(),
                    alunoId)).isInstanceOf(SQLException.class);
        }
    }

    private void update(Connection connection, String sql, Object... parameters) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < parameters.length; index++) {
                statement.setObject(index + 1, parameters[index]);
            }
            statement.executeUpdate();
        }
    }

    private Object query(Connection connection, String column, UUID alunoId) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT " + column + " FROM aluno WHERE id_aluno = ?")) {
            statement.setObject(1, alunoId);
            try (var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                return resultSet.getObject(column);
            }
        }
    }
}
