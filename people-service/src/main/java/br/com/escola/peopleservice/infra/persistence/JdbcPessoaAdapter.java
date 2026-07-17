package br.com.escola.peopleservice.infra.persistence;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.peopleservice.application.dto.PessoaResumoResponse;
import br.com.escola.peopleservice.application.port.out.PessoaPort;
import br.com.escola.peopleservice.infra.config.LeituraModeloMigrationProperties;

@Component
public class JdbcPessoaAdapter implements PessoaPort {

    private static final String QUERY = """
            SELECT id_pessoa, nome_completo, id_escola, escola_nome, ativo
            FROM pessoa
            WHERE id_pessoa = ?
              AND id_escola = ?
            """;

    private final LeituraModeloMigrationProperties properties;

    public JdbcPessoaAdapter(LeituraModeloMigrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public Optional<PessoaResumoResponse> buscarPessoaPorId(UUID pessoaId, UUID escolaId) {
        if (!StringUtils.hasText(properties.url())) {
            throw new IllegalStateException("identity-local-read-url-required");
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
                return Optional.of(new PessoaResumoResponse(
                        resultSet.getObject("id_pessoa", UUID.class),
                        resultSet.getString("nome_completo"),
                        resultSet.getObject("id_escola", UUID.class),
                        resultSet.getString("escola_nome"),
                        resultSet.getBoolean("ativo")));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("identity-local-read-failed", ex);
        }
    }

    private void loadDriver(String driverClassName) {
        if (!StringUtils.hasText(driverClassName)) {
            return;
        }
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("identity-local-read-driver-not-found", ex);
        }
    }
}


