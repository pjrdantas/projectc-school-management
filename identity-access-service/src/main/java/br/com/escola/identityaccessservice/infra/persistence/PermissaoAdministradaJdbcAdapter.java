package br.com.escola.identityaccessservice.infra.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.identityaccessservice.application.exception.ConflitoAcessoException;
import br.com.escola.identityaccessservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.identityaccessservice.application.model.PermissaoAdministrada;
import br.com.escola.identityaccessservice.application.port.out.PermissaoAdministradaPort;

@Component
public class PermissaoAdministradaJdbcAdapter implements PermissaoAdministradaPort {

    private final JdbcTemplate jdbcTemplate;

    public PermissaoAdministradaJdbcAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissaoAdministrada> listar() {
        return jdbcTemplate.query("""
                SELECT id_permissao, codigo, descricao, created_at
                FROM permissao
                ORDER BY codigo, id_permissao
                """, this::map);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissaoAdministrada buscar(UUID id) {
        return jdbcTemplate.query("""
                SELECT id_permissao, codigo, descricao, created_at
                FROM permissao
                WHERE id_permissao = ?
                """, this::map, id).stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Permissao nao encontrada"));
    }

    @Override
    @Transactional
    public PermissaoAdministrada salvar(UUID id, String codigo, String descricao) {
        try {
            int updated = jdbcTemplate.update("""
                    UPDATE permissao SET codigo = ?, descricao = ? WHERE id_permissao = ?
                    """, codigo, descricao, id);
            if (updated == 0) {
                jdbcTemplate.update("""
                        INSERT INTO permissao (id_permissao, codigo, descricao, created_at)
                        VALUES (?, ?, ?, ?)
                        """, id, codigo, descricao, LocalDateTime.now());
            }
            return buscar(id);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflitoAcessoException("Ja existe permissao com o codigo informado");
        }
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        Integer references = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM perfil_permissao WHERE id_permissao = ?", Integer.class, id);
        if (references != null && references > 0) {
            throw new ConflitoAcessoException("Permissao vinculada a perfil nao pode ser excluida");
        }
        if (jdbcTemplate.update("DELETE FROM permissao WHERE id_permissao = ?", id) == 0) {
            throw new RecursoNaoEncontradoException("Permissao nao encontrada");
        }
    }

    private PermissaoAdministrada map(ResultSet resultSet, int rowNum) throws SQLException {
        UUID id = UUID.fromString(resultSet.getObject("id_permissao").toString());
        String codigo = resultSet.getString("codigo");
        return new PermissaoAdministrada(
                id,
                codigo,
                codigo,
                resultSet.getString("descricao"),
                resultSet.getTimestamp("created_at").toLocalDateTime());
    }
}
