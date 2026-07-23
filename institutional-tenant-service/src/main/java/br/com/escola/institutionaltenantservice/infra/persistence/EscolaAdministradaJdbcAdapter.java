package br.com.escola.institutionaltenantservice.infra.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.institutionaltenantservice.application.exception.ConflitoTenantException;
import br.com.escola.institutionaltenantservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.institutionaltenantservice.application.model.EscolaAdministrada;
import br.com.escola.institutionaltenantservice.application.port.out.EscolaAdministradaPort;

@Component
public class EscolaAdministradaJdbcAdapter implements EscolaAdministradaPort {

    private static final String COLUMNS = """
            id_escola, nome, codigo_inep, cnpj, telefone, email,
            id_endereco, ativo, created_at, updated_at
            """;

    private final JdbcTemplate jdbcTemplate;

    public EscolaAdministradaJdbcAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EscolaAdministrada> listar() {
        return jdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM escola ORDER BY nome, id_escola",
                this::map);
    }

    @Override
    @Transactional(readOnly = true)
    public EscolaAdministrada buscar(UUID id) {
        return jdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM escola WHERE id_escola = ?",
                this::map,
                id).stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Escola nao encontrada"));
    }

    @Override
    @Transactional
    public EscolaAdministrada salvar(EscolaAdministrada escola) {
        int updated = jdbcTemplate.update("""
                UPDATE escola
                SET nome = ?, codigo_inep = ?, cnpj = ?, telefone = ?, email = ?,
                    id_endereco = ?, ativo = ?, updated_at = ?
                WHERE id_escola = ?
                """, escola.nome(), escola.codigoInep(), escola.cnpj(), escola.telefone(),
                escola.email(), escola.enderecoId(), escola.ativo(), escola.updatedAt(), escola.id());
        if (updated == 0) {
            jdbcTemplate.update("""
                    INSERT INTO escola (
                        id_escola, nome, codigo_inep, cnpj, telefone, email,
                        id_endereco, ativo, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, escola.id(), escola.nome(), escola.codigoInep(), escola.cnpj(),
                    escola.telefone(), escola.email(), escola.enderecoId(), escola.ativo(),
                    escola.createdAt(), escola.updatedAt());
        }
        return buscar(escola.id());
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        Integer links = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM usuario_escola WHERE id_escola = ?", Integer.class, id);
        if (links != null && links > 0) {
            throw new ConflitoTenantException("Escola vinculada a usuario nao pode ser excluida");
        }
        if (jdbcTemplate.update("DELETE FROM escola WHERE id_escola = ?", id) == 0) {
            throw new RecursoNaoEncontradoException("Escola nao encontrada");
        }
    }

    private EscolaAdministrada map(ResultSet resultSet, int rowNum) throws SQLException {
        return new EscolaAdministrada(
                readUuid(resultSet.getObject("id_escola")),
                resultSet.getString("nome"),
                resultSet.getString("codigo_inep"),
                resultSet.getString("cnpj"),
                resultSet.getString("telefone"),
                resultSet.getString("email"),
                readUuid(resultSet.getObject("id_endereco")),
                resultSet.getBoolean("ativo"),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getTimestamp("updated_at") == null
                        ? null
                        : resultSet.getTimestamp("updated_at").toLocalDateTime());
    }

    private UUID readUuid(Object value) {
        return value == null ? null : UUID.fromString(value.toString());
    }
}
