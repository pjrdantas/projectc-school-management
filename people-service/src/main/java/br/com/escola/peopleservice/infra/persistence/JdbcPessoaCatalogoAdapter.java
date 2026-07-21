package br.com.escola.peopleservice.infra.persistence;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.peopleservice.application.dto.PessoaCatalogoResponse;
import br.com.escola.peopleservice.application.port.out.PessoaCatalogoPort;
import br.com.escola.peopleservice.infra.config.PeoplePersistenceProperties;

@Component
public class JdbcPessoaCatalogoAdapter implements PessoaCatalogoPort {

    private final PeoplePersistenceProperties properties;

    public JdbcPessoaCatalogoAdapter(PeoplePersistenceProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<PessoaCatalogoResponse> listarTiposPessoa() {
        return query("""
                SELECT id_tipo_pessoa, codigo, descricao
                FROM tipo_pessoa
                ORDER BY codigo
                """);
    }

    @Override
    public List<PessoaCatalogoResponse> listarTiposEndereco() {
        return query("""
                SELECT id_tipo_endereco, codigo, descricao
                FROM tipo_endereco
                ORDER BY codigo
                """);
    }

    @Override
    public List<PessoaCatalogoResponse> listarStatusAluno() {
        return query("""
                SELECT id_status_aluno, codigo, descricao
                FROM status_aluno
                ORDER BY codigo
                """);
    }

    @Override
    public List<PessoaCatalogoResponse> listarParentescos() {
        return query("""
                SELECT id_parentesco, codigo, descricao
                FROM parentesco
                ORDER BY codigo
                """);
    }

    private List<PessoaCatalogoResponse> query(String sql) {
        if (!StringUtils.hasText(properties.url())) {
            throw new IllegalStateException("local-catalog-read-url-required");
        }
        loadDriver();
        try (var connection = DriverManager.getConnection(
                properties.url(),
                properties.username(),
                properties.password());
                var statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            var rows = new java.util.ArrayList<PessoaCatalogoResponse>();
            while (resultSet.next()) {
                rows.add(new PessoaCatalogoResponse(
                        resultSet.getObject(1, java.util.UUID.class),
                        resultSet.getString("codigo"),
                        resultSet.getString("descricao")));
            }
            return rows;
        } catch (SQLException ex) {
            throw new IllegalStateException("local-catalog-read-failed", ex);
        }
    }

    private void loadDriver() {
        if (!StringUtils.hasText(properties.driverClassName())) {
            return;
        }
        try {
            Class.forName(properties.driverClassName());
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("local-catalog-read-driver-not-found", ex);
        }
    }
}


