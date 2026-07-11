package br.com.escola.peopleservice.infra.persistence;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.peopleservice.application.dto.PessoaContatoLocalReadResponse;
import br.com.escola.peopleservice.application.port.out.PeopleContactLocalReadPort;
import br.com.escola.peopleservice.infra.config.PeopleLocalReadModelSchemaMigrationProperties;

@Component
public class JdbcPeopleContactLocalReadAdapter implements PeopleContactLocalReadPort {

    private static final String QUERY = """
            SELECT id_pessoa, id_escola, email, telefone, ativo
            FROM pessoa
            WHERE id_pessoa = ?
              AND id_escola = ?
            """;

    private final PeopleLocalReadModelSchemaMigrationProperties properties;

    public JdbcPeopleContactLocalReadAdapter(PeopleLocalReadModelSchemaMigrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public Optional<PessoaContatoLocalReadResponse> buscarContatoPorPessoa(UUID pessoaId, UUID escolaId) {
        if (!StringUtils.hasText(properties.url())) {
            throw new IllegalStateException("contact-local-read-url-required");
        }
        loadDriver(properties.driverClassName());

        try (var connection = DriverManager.getConnection(
                properties.url(),
                properties.username(),
                properties.password());
                var statement = connection.prepareStatement(QUERY)) {
            statement.setObject(1, pessoaId);
            statement.setObject(2, escolaId);
            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(new PessoaContatoLocalReadResponse(
                        resultSet.getObject("id_pessoa", UUID.class),
                        resultSet.getObject("id_escola", UUID.class),
                        resultSet.getString("email"),
                        resultSet.getString("telefone"),
                        resultSet.getBoolean("ativo")));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("contact-local-read-failed", ex);
        }
    }

    private void loadDriver(String driverClassName) {
        if (!StringUtils.hasText(driverClassName)) {
            return;
        }
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("contact-local-read-driver-not-found", ex);
        }
    }
}
