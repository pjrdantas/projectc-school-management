package br.com.escola.peopleservice.infra.persistence;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.peopleservice.application.dto.PessoaResponsavelVinculoResponse;
import br.com.escola.peopleservice.application.port.out.ResponsavelPessoaPort;
import br.com.escola.peopleservice.infra.config.PeopleReadModelMigrationProperties;

@Component
public class JdbcResponsavelPessoaAdapter implements ResponsavelPessoaPort {

    private static final String QUERY = """
            SELECT id_responsavel, id_pessoa, id_escola
            FROM responsavel
            WHERE id_responsavel = ?
              AND id_escola = ?
            """;

    private final PeopleReadModelMigrationProperties properties;

    public JdbcResponsavelPessoaAdapter(PeopleReadModelMigrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public Optional<PessoaResponsavelVinculoResponse> buscarVinculoPorResponsavelId(UUID responsavelId, UUID escolaId) {
        if (!StringUtils.hasText(properties.url())) {
            throw new IllegalStateException("responsible-pessoa-local-read-url-required");
        }
        loadDriver(properties.driverClassName());

        try (var connection = DriverManager.getConnection(
                properties.url(),
                properties.username(),
                properties.password());
                var statement = connection.prepareStatement(QUERY)) {
            statement.setObject(1, responsavelId);
            statement.setObject(2, escolaId);
            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                UUID pessoaId = resultSet.getObject("id_pessoa", UUID.class);
                if (pessoaId == null) {
                    return Optional.empty();
                }
                return Optional.of(new PessoaResponsavelVinculoResponse(
                        resultSet.getObject("id_responsavel", UUID.class),
                        pessoaId,
                        resultSet.getObject("id_escola", UUID.class)));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("responsible-pessoa-local-read-failed", ex);
        }
    }

    private void loadDriver(String driverClassName) {
        if (!StringUtils.hasText(driverClassName)) {
            return;
        }
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("responsible-pessoa-local-read-driver-not-found", ex);
        }
    }
}

