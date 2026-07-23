package br.com.escola.peopleservice.infra.persistence;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.peopleservice.application.dto.PessoaFuncionarioResumoResponse;
import br.com.escola.peopleservice.application.port.out.PessoaFuncionarioResumoPort;
import br.com.escola.peopleservice.infra.config.PersistenceProperties;

@Component
public class JdbcPessoaFuncionarioResumoAdapter implements PessoaFuncionarioResumoPort {

    private static final String BASE_SELECT = """
            SELECT id_funcionario,
                   id_pessoa,
                   id_escola,
                   nome_completo,
                   cargo_descricao,
                   ativo,
                   created_at
            FROM people_funcionario_read_model
            WHERE id_escola = ?
            """;

    private final PersistenceProperties properties;

    public JdbcPessoaFuncionarioResumoAdapter(PersistenceProperties properties) {
        this.properties = properties;
    }

    @Override
    public Optional<PessoaFuncionarioResumoResponse> buscarFuncionarioPorId(UUID funcionarioId, UUID escolaId) {
        List<PessoaFuncionarioResumoResponse> funcionarios = consultar(
                BASE_SELECT + " AND id_funcionario = ? ORDER BY created_at DESC, id_funcionario",
                escolaId,
                funcionarioId);
        if (funcionarios.size() > 1) {
            throw new IllegalStateException("funcionario-internal-summary-duplicate-id");
        }
        return funcionarios.stream().findFirst();
    }

    @Override
    public List<PessoaFuncionarioResumoResponse> listarFuncionariosAtivosPorEscola(UUID escolaId) {
        return consultar(
                BASE_SELECT + " AND ativo = TRUE ORDER BY nome_completo ASC, id_funcionario",
                escolaId,
                null);
    }

    private List<PessoaFuncionarioResumoResponse> consultar(
            String sql,
            UUID escolaId,
            UUID funcionarioId) {
        if (!StringUtils.hasText(properties.url())) {
            throw new IllegalStateException("funcionario-internal-summary-local-read-url-required");
        }
        loadDriver(properties.driverClassName());

        try (var connection = DriverManager.getConnection(
                properties.url(),
                properties.username(),
                properties.password());
                var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, escolaId);
            if (funcionarioId != null) {
                statement.setObject(2, funcionarioId);
            }
            try (var resultSet = statement.executeQuery()) {
                List<PessoaFuncionarioResumoResponse> funcionarios = new ArrayList<>();
                while (resultSet.next()) {
                    funcionarios.add(new PessoaFuncionarioResumoResponse(
                            resultSet.getObject("id_funcionario", UUID.class),
                            resultSet.getObject("id_pessoa", UUID.class),
                            resultSet.getObject("id_escola", UUID.class),
                            resultSet.getString("nome_completo"),
                            resultSet.getString("cargo_descricao"),
                            resultSet.getBoolean("ativo")));
                }
                return List.copyOf(funcionarios);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("funcionario-internal-summary-local-read-failed", ex);
        }
    }

    private void loadDriver(String driverClassName) {
        if (!StringUtils.hasText(driverClassName)) {
            return;
        }
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("funcionario-internal-summary-local-read-driver-not-found", ex);
        }
    }
}


