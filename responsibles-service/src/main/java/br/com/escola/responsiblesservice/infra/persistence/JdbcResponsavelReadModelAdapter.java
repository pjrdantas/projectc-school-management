package br.com.escola.responsiblesservice.infra.persistence;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.responsiblesservice.application.dto.ResponsavelAlunoVinculadoReadModelResponse;
import br.com.escola.responsiblesservice.application.dto.ResponsavelReadModelResponse;
import br.com.escola.responsiblesservice.application.port.out.ResponsavelLocalReadPort;
import br.com.escola.responsiblesservice.infra.config.PersistenceProperties;

@Component
public class JdbcResponsavelReadModelAdapter implements ResponsavelLocalReadPort {

    private static final String LIST_QUERY = """
            SELECT id_responsavel, nome_completo, cpf, email, telefone, rg,
                   cep, logradouro, numero, complemento, bairro, cidade, uf,
                   id_escola, escola_nome, created_at
            FROM responsavel
            WHERE id_escola = ?
              AND ativo = TRUE
              AND (? IS NULL OR LOWER(nome_completo) LIKE LOWER(?))
              AND (? IS NULL OR cpf = ?)
            ORDER BY nome_completo
            """;

    private static final String DETAIL_QUERY = """
            SELECT id_responsavel, nome_completo, cpf, email, telefone, rg,
                   cep, logradouro, numero, complemento, bairro, cidade, uf,
                   id_escola, escola_nome, created_at
            FROM responsavel
            WHERE id_responsavel = ?
              AND id_escola = ?
              AND ativo = TRUE
            """;

    private static final String STUDENT_LINK_QUERY = """
            SELECT r.id_responsavel,
                   r.nome_completo,
                   r.cpf,
                   r.email,
                   r.telefone,
                   r.rg,
                   r.cep,
                   r.logradouro,
                   r.numero,
                   r.complemento,
                   r.bairro,
                   r.cidade,
                   r.uf,
                   p.codigo AS parentesco_codigo,
                   ar.responsavel_financeiro,
                   ar.responsavel_pedagogico,
                   ar.autorizado_retirar,
                   r.created_at
            FROM aluno_responsavel ar
              JOIN responsavel r ON r.id_responsavel = ar.id_responsavel
              LEFT JOIN parentesco p ON p.id_parentesco = ar.id_parentesco
            WHERE ar.id_aluno = ?
              AND r.id_escola = ?
              AND r.ativo = TRUE
            ORDER BY r.nome_completo
            """;

    private final PersistenceProperties properties;

    public JdbcResponsavelReadModelAdapter(PersistenceProperties properties) {
        this.properties = properties;
    }

    @Override
    public Optional<List<ResponsavelReadModelResponse>> listarResponsaveis(
            UUID escolaId,
            String nome,
            String cpf) {
        requireUrl();
        loadDriver();

        String nomeFiltro = normalizeLike(nome);
        String cpfFiltro = normalizeValue(cpf);
        try (var connection = DriverManager.getConnection(properties.url(), properties.username(), properties.password());
                var statement = connection.prepareStatement(LIST_QUERY)) {
            statement.setObject(1, escolaId);
            statement.setString(2, nomeFiltro);
            statement.setString(3, nomeFiltro == null ? null : "%" + nomeFiltro + "%");
            statement.setString(4, cpfFiltro);
            statement.setString(5, cpfFiltro);

            try (var resultSet = statement.executeQuery()) {
                List<ResponsavelReadModelResponse> responsaveis = new ArrayList<>();
                while (resultSet.next()) {
                    responsaveis.add(map(resultSet));
                }
                return Optional.of(responsaveis);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("responsibles-local-read-list-failed", exception);
        }
    }

    @Override
    public Optional<ResponsavelReadModelResponse> buscarResponsavelPorId(UUID responsavelId, UUID escolaId) {
        requireUrl();
        loadDriver();

        try (var connection = DriverManager.getConnection(properties.url(), properties.username(), properties.password());
                var statement = connection.prepareStatement(DETAIL_QUERY)) {
            statement.setObject(1, responsavelId);
            statement.setObject(2, escolaId);
            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("responsibles-local-read-detail-failed", exception);
        }
    }

    @Override
    public Optional<List<ResponsavelAlunoVinculadoReadModelResponse>> listarResponsaveisPorAluno(
            UUID alunoId,
            UUID escolaId) {
        requireUrl();
        loadDriver();

        try (var connection = DriverManager.getConnection(properties.url(), properties.username(), properties.password());
                var statement = connection.prepareStatement(STUDENT_LINK_QUERY)) {
            statement.setObject(1, alunoId);
            statement.setObject(2, escolaId);
            try (var resultSet = statement.executeQuery()) {
                List<ResponsavelAlunoVinculadoReadModelResponse> responsaveis = new ArrayList<>();
                while (resultSet.next()) {
                    responsaveis.add(mapStudentLink(resultSet));
                }
                return responsaveis.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(responsaveis));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("responsibles-local-read-student-link-failed", exception);
        }
    }

    private ResponsavelReadModelResponse map(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new ResponsavelReadModelResponse(
                resultSet.getObject("id_responsavel", UUID.class),
                resultSet.getString("nome_completo"),
                resultSet.getString("cpf"),
                resultSet.getString("email"),
                resultSet.getString("telefone"),
                resultSet.getString("rg"),
                resultSet.getString("cep"),
                resultSet.getString("logradouro"),
                resultSet.getString("numero"),
                resultSet.getString("complemento"),
                resultSet.getString("bairro"),
                resultSet.getString("cidade"),
                resultSet.getString("uf"),
                resultSet.getObject("id_escola", UUID.class),
                resultSet.getString("escola_nome"),
                createdAt == null ? null : createdAt.toLocalDateTime());
    }

    private ResponsavelAlunoVinculadoReadModelResponse mapStudentLink(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new ResponsavelAlunoVinculadoReadModelResponse(
                resultSet.getObject("id_responsavel", UUID.class),
                resultSet.getString("nome_completo"),
                resultSet.getString("cpf"),
                resultSet.getString("email"),
                resultSet.getString("telefone"),
                resultSet.getString("rg"),
                resultSet.getString("cep"),
                resultSet.getString("logradouro"),
                resultSet.getString("numero"),
                resultSet.getString("complemento"),
                resultSet.getString("bairro"),
                resultSet.getString("cidade"),
                resultSet.getString("uf"),
                resultSet.getString("parentesco_codigo"),
                getBoolean(resultSet, "responsavel_financeiro"),
                getBoolean(resultSet, "responsavel_pedagogico"),
                getBoolean(resultSet, "autorizado_retirar"),
                createdAt == null ? null : createdAt.toLocalDateTime());
    }

    private void requireUrl() {
        if (!StringUtils.hasText(properties.url())) {
            throw new IllegalStateException("responsibles-local-read-url-required");
        }
    }

    private void loadDriver() {
        if (!StringUtils.hasText(properties.driverClassName())) {
            return;
        }
        try {
            Class.forName(properties.driverClassName());
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("responsibles-local-read-driver-not-found", exception);
        }
    }

    private String normalizeLike(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeValue(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Boolean getBoolean(ResultSet resultSet, String column) throws SQLException {
        boolean value = resultSet.getBoolean(column);
        return resultSet.wasNull() ? null : value;
    }
}

