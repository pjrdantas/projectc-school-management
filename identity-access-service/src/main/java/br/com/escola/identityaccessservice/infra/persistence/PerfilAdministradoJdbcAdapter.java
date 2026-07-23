package br.com.escola.identityaccessservice.infra.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.identityaccessservice.application.exception.ConflitoAcessoException;
import br.com.escola.identityaccessservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.identityaccessservice.application.model.PerfilAdministrado;
import br.com.escola.identityaccessservice.application.model.PermissaoAdministrada;
import br.com.escola.identityaccessservice.application.port.out.PerfilAdministradoPort;

@Component
public class PerfilAdministradoJdbcAdapter implements PerfilAdministradoPort {

    private final JdbcTemplate jdbcTemplate;

    public PerfilAdministradoJdbcAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfilAdministrado> listar() {
        return jdbcTemplate.query("""
                SELECT id_perfil, codigo, nome, descricao, created_at
                FROM perfil
                ORDER BY codigo, id_perfil
                """, this::mapPerfil);
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilAdministrado buscar(UUID id) {
        return jdbcTemplate.query("""
                SELECT id_perfil, codigo, nome, descricao, created_at
                FROM perfil
                WHERE id_perfil = ?
                """, this::mapPerfil, id).stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Perfil nao encontrado"));
    }

    @Override
    @Transactional
    public PerfilAdministrado salvar(
            UUID id,
            String codigo,
            String nome,
            String descricao,
            Set<UUID> permissaoIds) {
        validarPermissoes(permissaoIds);
        try {
            int updated = jdbcTemplate.update("""
                    UPDATE perfil SET codigo = ?, nome = ?, descricao = ? WHERE id_perfil = ?
                    """, codigo, nome, descricao, id);
            if (updated == 0) {
                jdbcTemplate.update("""
                        INSERT INTO perfil (id_perfil, codigo, nome, descricao, created_at)
                        VALUES (?, ?, ?, ?, ?)
                        """, id, codigo, nome, descricao, LocalDateTime.now());
            }
            jdbcTemplate.update("DELETE FROM perfil_permissao WHERE id_perfil = ?", id);
            permissaoIds.forEach(permissaoId -> jdbcTemplate.update("""
                    INSERT INTO perfil_permissao (
                        id_perfil_permissao, id_perfil, id_permissao
                    ) VALUES (?, ?, ?)
                    """, UUID.randomUUID(), id, permissaoId));
            return buscar(id);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflitoAcessoException("Ja existe perfil com o codigo informado");
        }
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        Integer references = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM usuario_perfil WHERE id_perfil = ?", Integer.class, id);
        if (references != null && references > 0) {
            throw new ConflitoAcessoException("Perfil atribuido a usuario nao pode ser excluido");
        }
        jdbcTemplate.update("DELETE FROM perfil_permissao WHERE id_perfil = ?", id);
        if (jdbcTemplate.update("DELETE FROM perfil WHERE id_perfil = ?", id) == 0) {
            throw new RecursoNaoEncontradoException("Perfil nao encontrado");
        }
    }

    private void validarPermissoes(Set<UUID> permissaoIds) {
        if (permissaoIds.isEmpty()) {
            return;
        }
        Integer existing = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM permissao WHERE id_permissao IN ("
                        + String.join(",", permissaoIds.stream().map(id -> "?").toList()) + ")",
                Integer.class,
                permissaoIds.toArray());
        if (existing == null || existing != permissaoIds.size()) {
            throw new RecursoNaoEncontradoException("Uma ou mais permissoes nao foram encontradas");
        }
    }

    private PerfilAdministrado mapPerfil(ResultSet resultSet, int rowNum) throws SQLException {
        UUID id = UUID.fromString(resultSet.getObject("id_perfil").toString());
        return new PerfilAdministrado(
                id,
                resultSet.getString("codigo"),
                resultSet.getString("nome"),
                resultSet.getString("descricao"),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                listarPermissoes(id));
    }

    private List<PermissaoAdministrada> listarPermissoes(UUID perfilId) {
        return jdbcTemplate.query("""
                SELECT p.id_permissao, p.codigo, p.descricao, p.created_at
                FROM perfil_permissao pp
                JOIN permissao p ON p.id_permissao = pp.id_permissao
                WHERE pp.id_perfil = ?
                ORDER BY p.codigo, p.id_permissao
                """, (resultSet, rowNum) -> {
                    UUID id = UUID.fromString(resultSet.getObject("id_permissao").toString());
                    String codigo = resultSet.getString("codigo");
                    return new PermissaoAdministrada(
                            id,
                            codigo,
                            codigo,
                            resultSet.getString("descricao"),
                            resultSet.getTimestamp("created_at").toLocalDateTime());
                }, perfilId);
    }
}
