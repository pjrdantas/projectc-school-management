package br.com.escola.peopleservice.infra.persistence;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.peopleservice.application.dto.PessoaAlunoVinculoResponse;
import br.com.escola.peopleservice.application.port.out.AlunoPessoaPort;
import br.com.escola.peopleservice.infra.config.PeopleReadModelMigrationProperties;

@Component
public class JdbcAlunoPessoaAdapter implements AlunoPessoaPort {

    private static final String QUERY = """
            SELECT id_aluno, id_pessoa, id_escola
            FROM aluno
            WHERE id_aluno = ?
              AND id_escola = ?
            """;

    private final PeopleReadModelMigrationProperties properties;

    public JdbcAlunoPessoaAdapter(PeopleReadModelMigrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public Optional<PessoaAlunoVinculoResponse> buscarVinculoPorAlunoId(UUID alunoId, UUID escolaId) {
        if (!StringUtils.hasText(properties.url())) {
            throw new IllegalStateException("student-pessoa-local-read-url-required");
        }
        loadDriver(properties.driverClassName());

        try (var connection = DriverManager.getConnection(
                properties.url(),
                properties.username(),
                properties.password());
                var statement = connection.prepareStatement(QUERY)) {
            statement.setObject(1, alunoId);
            statement.setObject(2, escolaId);
            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                UUID pessoaId = resultSet.getObject("id_pessoa", UUID.class);
                if (pessoaId == null) {
                    return Optional.empty();
                }
                return Optional.of(new PessoaAlunoVinculoResponse(
                        resultSet.getObject("id_aluno", UUID.class),
                        pessoaId,
                        resultSet.getObject("id_escola", UUID.class)));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("student-pessoa-local-read-failed", ex);
        }
    }

    private void loadDriver(String driverClassName) {
        if (!StringUtils.hasText(driverClassName)) {
            return;
        }
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("student-pessoa-local-read-driver-not-found", ex);
        }
    }
}

