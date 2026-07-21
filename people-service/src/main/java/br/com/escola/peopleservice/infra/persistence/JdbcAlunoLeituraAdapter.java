package br.com.escola.peopleservice.infra.persistence;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.peopleservice.application.model.AlunoConsulta;
import br.com.escola.peopleservice.application.port.out.AlunoLeituraPort;
import br.com.escola.peopleservice.infra.config.PeoplePersistenceProperties;

@Component
public class JdbcAlunoLeituraAdapter implements AlunoLeituraPort {

    private static final String SELECT = """
            SELECT a.id_aluno,
                   p.nome_completo,
                   p.cpf,
                   p.email,
                   p.telefone,
                   p.data_nascimento,
                   p.rg,
                   p.orgao_emissor_rg,
                   p.uf_rg,
                   p.nacionalidade,
                   p.naturalidade,
                   p.sexo,
                   p.nome_social,
                   e.cep,
                   e.logradouro,
                   e.numero,
                   e.complemento,
                   e.bairro,
                   e.cidade,
                   e.uf,
                   COALESCE(sa.codigo, 'ATIVO') AS status_aluno,
                   p.id_escola,
                   p.escola_nome,
                   a.created_at
            FROM aluno a
            JOIN pessoa p ON p.id_pessoa = a.id_pessoa
            LEFT JOIN status_aluno sa ON sa.id_status_aluno = a.id_status_aluno
            LEFT JOIN pessoa_endereco pe ON pe.id_pessoa = p.id_pessoa AND pe.principal = TRUE
            LEFT JOIN endereco e ON e.id_endereco = pe.id_endereco
            WHERE a.id_escola = ? AND p.id_escola = ? AND a.ativo = TRUE AND p.ativo = TRUE
            """;

    private final PeoplePersistenceProperties properties;

    public JdbcAlunoLeituraAdapter(PeoplePersistenceProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<AlunoConsulta> listar(String nome, UUID escolaId) {
        validarConfiguracao();
        carregarDriver();
        String filtro = normalizar(nome);
        String sql = SELECT + " AND (? IS NULL OR LOWER(p.nome_completo) LIKE LOWER(?))"
                + " ORDER BY p.nome_completo, a.id_aluno";
        try (var connection = DriverManager.getConnection(
                properties.url(), properties.username(), properties.password());
                var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, escolaId);
            statement.setObject(2, escolaId);
            statement.setString(3, filtro);
            statement.setString(4, filtro == null ? null : "%" + filtro + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                List<AlunoConsulta> alunos = new ArrayList<>();
                UUID ultimoAlunoId = null;
                while (resultSet.next()) {
                    UUID alunoId = resultSet.getObject("id_aluno", UUID.class);
                    if (alunoId.equals(ultimoAlunoId)) {
                        throw new IllegalStateException("Aluno possui mais de um endereco principal");
                    }
                    alunos.add(mapear(resultSet));
                    ultimoAlunoId = alunoId;
                }
                return List.copyOf(alunos);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Falha ao listar alunos no banco de pessoas", exception);
        }
    }

    @Override
    public Optional<AlunoConsulta> buscar(UUID alunoId, UUID escolaId) {
        validarConfiguracao();
        carregarDriver();
        String sql = SELECT + " AND a.id_aluno = ?";
        try (var connection = DriverManager.getConnection(
                properties.url(), properties.username(), properties.password());
                var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, escolaId);
            statement.setObject(2, escolaId);
            statement.setObject(3, alunoId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                AlunoConsulta aluno = mapear(resultSet);
                if (resultSet.next()) {
                    throw new IllegalStateException("Aluno possui mais de um endereco principal");
                }
                return Optional.of(aluno);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Falha ao buscar aluno no banco de pessoas", exception);
        }
    }

    private AlunoConsulta mapear(ResultSet resultSet) throws SQLException {
        return new AlunoConsulta(
                resultSet.getObject("id_aluno", UUID.class),
                resultSet.getString("nome_completo"),
                resultSet.getString("cpf"),
                resultSet.getString("email"),
                resultSet.getString("telefone"),
                localDate(resultSet, "data_nascimento"),
                resultSet.getString("rg"),
                resultSet.getString("orgao_emissor_rg"),
                resultSet.getString("uf_rg"),
                resultSet.getString("nacionalidade"),
                resultSet.getString("naturalidade"),
                resultSet.getString("sexo"),
                resultSet.getString("nome_social"),
                resultSet.getString("cep"),
                resultSet.getString("logradouro"),
                resultSet.getString("numero"),
                resultSet.getString("complemento"),
                resultSet.getString("bairro"),
                resultSet.getString("cidade"),
                resultSet.getString("uf"),
                resultSet.getString("status_aluno"),
                resultSet.getObject("id_escola", UUID.class),
                resultSet.getString("escola_nome"),
                localDateTime(resultSet, "created_at"));
    }

    private LocalDate localDate(ResultSet resultSet, String column) throws SQLException {
        var value = resultSet.getDate(column);
        return value == null ? null : value.toLocalDate();
    }

    private LocalDateTime localDateTime(ResultSet resultSet, String column) throws SQLException {
        Timestamp value = resultSet.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }

    private String normalizar(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private void validarConfiguracao() {
        if (!StringUtils.hasText(properties.url())) {
            throw new IllegalStateException("Banco de pessoas nao configurado");
        }
    }

    private void carregarDriver() {
        if (!StringUtils.hasText(properties.driverClassName())) {
            return;
        }
        try {
            Class.forName(properties.driverClassName());
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Driver do banco de pessoas nao encontrado", exception);
        }
    }
}
