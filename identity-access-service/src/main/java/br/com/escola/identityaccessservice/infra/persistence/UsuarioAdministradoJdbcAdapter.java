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
import br.com.escola.identityaccessservice.application.model.PerfilVinculado;
import br.com.escola.identityaccessservice.application.model.UsuarioAdministrado;
import br.com.escola.identityaccessservice.application.port.out.UsuarioAdministradoPort;

@Component
public class UsuarioAdministradoJdbcAdapter implements UsuarioAdministradoPort {

    private final JdbcTemplate jdbcTemplate;

    public UsuarioAdministradoJdbcAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioAdministrado> listar() {
        return jdbcTemplate.query("""
                SELECT id_usuario, username, nome, email, ativo, id_escola, created_at
                FROM usuario
                ORDER BY nome, username, id_usuario
                """, this::mapUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioAdministrado buscar(UUID id) {
        return jdbcTemplate.query("""
                SELECT id_usuario, username, nome, email, ativo, id_escola, created_at
                FROM usuario
                WHERE id_usuario = ?
                """, this::mapUsuario, id).stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));
    }

    @Override
    @Transactional
    public UsuarioAdministrado salvar(
            UUID id,
            String username,
            String nome,
            String email,
            String senhaProtegida,
            boolean ativo,
            UUID escolaId,
            Set<UUID> perfilIds) {
        validarPerfis(perfilIds);
        try {
            int updated = jdbcTemplate.update("""
                    UPDATE usuario
                    SET username = ?, nome = ?, email = ?, senha_hash = ?, ativo = ?, id_escola = ?
                    WHERE id_usuario = ?
                    """, username, nome, email, senhaProtegida, ativo, escolaId, id);
            if (updated == 0) {
                jdbcTemplate.update("""
                        INSERT INTO usuario (
                            id_usuario, username, nome, email, senha_hash,
                            ativo, id_escola, created_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """, id, username, nome, email, senhaProtegida,
                        ativo, escolaId, LocalDateTime.now());
            }
            jdbcTemplate.update("DELETE FROM usuario_perfil WHERE id_usuario = ?", id);
            perfilIds.forEach(perfilId -> jdbcTemplate.update("""
                    INSERT INTO usuario_perfil (id_usuario_perfil, id_usuario, id_perfil)
                    VALUES (?, ?, ?)
                    """, UUID.randomUUID(), id, perfilId));
            return buscar(id);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflitoAcessoException("Ja existe usuario com username ou email informado");
        }
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        usuarioExiste(id);
        jdbcTemplate.update("DELETE FROM sessao_autenticacao WHERE id_usuario = ?", id);
        jdbcTemplate.update("DELETE FROM usuario_perfil WHERE id_usuario = ?", id);
        try {
            jdbcTemplate.update("DELETE FROM usuario WHERE id_usuario = ?", id);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflitoAcessoException("Usuario possui vinculos e nao pode ser excluido");
        }
    }

    private void validarPerfis(Set<UUID> perfilIds) {
        Integer existing = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM perfil WHERE id_perfil IN ("
                        + String.join(",", perfilIds.stream().map(id -> "?").toList()) + ")",
                Integer.class,
                perfilIds.toArray());
        if (existing == null || existing != perfilIds.size()) {
            throw new RecursoNaoEncontradoException("Um ou mais perfis nao foram encontrados");
        }
    }

    private void usuarioExiste(UUID id) {
        Integer existing = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM usuario WHERE id_usuario = ?", Integer.class, id);
        if (existing == null || existing == 0) {
            throw new RecursoNaoEncontradoException("Usuario nao encontrado");
        }
    }

    private UsuarioAdministrado mapUsuario(ResultSet resultSet, int rowNum) throws SQLException {
        UUID id = UUID.fromString(resultSet.getObject("id_usuario").toString());
        Object escola = resultSet.getObject("id_escola");
        return new UsuarioAdministrado(
                id,
                resultSet.getString("username"),
                resultSet.getString("nome"),
                resultSet.getString("email"),
                resultSet.getBoolean("ativo"),
                escola == null ? null : UUID.fromString(escola.toString()),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                listarPerfis(id));
    }

    private List<PerfilVinculado> listarPerfis(UUID usuarioId) {
        return jdbcTemplate.query("""
                SELECT p.id_perfil, p.codigo, p.nome
                FROM usuario_perfil up
                JOIN perfil p ON p.id_perfil = up.id_perfil
                WHERE up.id_usuario = ?
                ORDER BY p.codigo, p.id_perfil
                """, (resultSet, rowNum) -> new PerfilVinculado(
                        UUID.fromString(resultSet.getObject("id_perfil").toString()),
                        resultSet.getString("codigo"),
                        resultSet.getString("nome")), usuarioId);
    }
}
