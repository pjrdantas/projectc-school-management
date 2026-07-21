package br.com.escola.peopleservice.infra.persistence;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.peopleservice.application.dto.PessoaEnderecoResponse;
import br.com.escola.peopleservice.application.port.out.PessoaEnderecoPort;
import br.com.escola.peopleservice.infra.config.PeoplePersistenceProperties;

@Component
public class JdbcPessoaEnderecoAdapter implements PessoaEnderecoPort {

    private static final String BASE_SELECT = """
            SELECT pe.id_pessoa_endereco,
                   pe.id_pessoa,
                   pe.id_endereco,
                   pe.id_tipo_endereco,
                   te.codigo AS tipo_endereco_codigo,
                   te.descricao AS tipo_endereco_descricao,
                   pe.principal,
                   e.cep,
                   e.logradouro,
                   e.numero,
                   e.complemento,
                   e.bairro,
                   e.cidade,
                   e.uf
            FROM pessoa_endereco pe
              JOIN pessoa p ON p.id_pessoa = pe.id_pessoa
              JOIN endereco e ON e.id_endereco = pe.id_endereco
              JOIN tipo_endereco te ON te.id_tipo_endereco = pe.id_tipo_endereco
            WHERE pe.id_pessoa = ?
              AND p.id_escola = ?
            """;

    private final PeoplePersistenceProperties properties;

    public JdbcPessoaEnderecoAdapter(PeoplePersistenceProperties properties) {
        this.properties = properties;
    }

    @Override
    public Optional<PessoaEnderecoResponse> buscarEnderecoPrincipalPorPessoa(UUID pessoaId, UUID escolaId) {
        List<PessoaEnderecoResponse> enderecos = consultar(
                BASE_SELECT + " AND pe.principal = TRUE ORDER BY pe.created_at DESC, pe.id_pessoa_endereco",
                pessoaId,
                escolaId);
        if (enderecos.size() > 1) {
            throw new IllegalStateException("address-principal-rule-violated");
        }
        return enderecos.stream().findFirst();
    }

    @Override
    public List<PessoaEnderecoResponse> listarEnderecosPorPessoa(UUID pessoaId, UUID escolaId) {
        return consultar(
                BASE_SELECT + " ORDER BY pe.principal DESC, pe.created_at DESC, pe.id_pessoa_endereco",
                pessoaId,
                escolaId);
    }

    private List<PessoaEnderecoResponse> consultar(String sql, UUID pessoaId, UUID escolaId) {
        if (!StringUtils.hasText(properties.url())) {
            throw new IllegalStateException("address-local-read-url-required");
        }
        loadDriver(properties.driverClassName());

        try (var connection = DriverManager.getConnection(
                properties.url(),
                properties.username(),
                properties.password());
                var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, pessoaId);
            statement.setObject(2, escolaId);
            try (var resultSet = statement.executeQuery()) {
                List<PessoaEnderecoResponse> enderecos = new ArrayList<>();
                while (resultSet.next()) {
                    enderecos.add(new PessoaEnderecoResponse(
                            resultSet.getObject("id_pessoa_endereco", UUID.class),
                            resultSet.getObject("id_pessoa", UUID.class),
                            resultSet.getObject("id_endereco", UUID.class),
                            resultSet.getObject("id_tipo_endereco", UUID.class),
                            resultSet.getString("tipo_endereco_codigo"),
                            resultSet.getString("tipo_endereco_descricao"),
                            resultSet.getBoolean("principal"),
                            resultSet.getString("cep"),
                            resultSet.getString("logradouro"),
                            resultSet.getString("numero"),
                            resultSet.getString("complemento"),
                            resultSet.getString("bairro"),
                            resultSet.getString("cidade"),
                            resultSet.getString("uf")));
                }
                return List.copyOf(enderecos);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("address-local-read-failed", ex);
        }
    }

    private void loadDriver(String driverClassName) {
        if (!StringUtils.hasText(driverClassName)) {
            return;
        }
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("address-local-read-driver-not-found", ex);
        }
    }
}


