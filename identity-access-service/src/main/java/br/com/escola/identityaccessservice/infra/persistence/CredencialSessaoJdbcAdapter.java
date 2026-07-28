package br.com.escola.identityaccessservice.infra.persistence;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.identityaccessservice.application.exception.CredenciaisInvalidasException;
import br.com.escola.identityaccessservice.application.exception.TokenInvalidoException;
import br.com.escola.identityaccessservice.application.model.SessaoAutenticada;
import br.com.escola.identityaccessservice.application.port.out.CredencialSessaoPort;

@Component
public class CredencialSessaoJdbcAdapter implements CredencialSessaoPort {

    private static final int ACCESS_MINUTES = 30;
    private static final int REFRESH_DAYS = 7;

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public CredencialSessaoJdbcAdapter(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public SessaoAutenticada autenticar(String login, String senha) {
        UsuarioLinha usuario = jdbcTemplate.query("""
                SELECT id_usuario, username, nome, senha_hash, id_escola
                FROM usuario
                WHERE ativo = true
                  AND (lower(trim(username)) = lower(trim(?))
                       OR lower(trim(email)) = lower(trim(?)))
                """, this::mapUsuario, login, login).stream()
                .findFirst()
                .orElseThrow(this::credenciaisInvalidas);

        if (!passwordEncoder.matches(senha, usuario.senhaHash())) {
            throw credenciaisInvalidas();
        }
        return criarSessao(usuario);
    }

    @Override
    @Transactional
    public SessaoAutenticada renovar(String refreshToken) {
        SessaoLinha sessao = jdbcTemplate.query("""
                SELECT s.id_sessao_autenticacao, s.id_escola, u.id_usuario, u.username, u.nome, u.senha_hash
                FROM sessao_autenticacao s
                JOIN usuario u ON u.id_usuario = s.id_usuario
                WHERE s.refresh_token_hash = ?
                  AND s.revogado = false
                  AND s.expira_em > ?
                  AND u.ativo = true
                """, this::mapSessao, hashToken(refreshToken), LocalDateTime.now()).stream()
                .findFirst()
                .orElseThrow(() -> new TokenInvalidoException("Refresh token invalido ou expirado"));

        String accessToken = gerarToken();
        String novoRefreshToken = gerarToken();
        jdbcTemplate.update("""
                UPDATE sessao_autenticacao
                SET refresh_token_hash = ?, access_token_hash = ?, expira_em = ?, access_expira_em = ?
                WHERE id_sessao_autenticacao = ?
                """, hashToken(novoRefreshToken), hashToken(accessToken),
                LocalDateTime.now().plusDays(REFRESH_DAYS),
                LocalDateTime.now().plusMinutes(ACCESS_MINUTES), sessao.sessaoId());
        return response(accessToken, novoRefreshToken, sessao.usuario());
    }

    @Override
    @Transactional
    public void encerrar(String refreshToken) {
        int updated = jdbcTemplate.update("""
                UPDATE sessao_autenticacao
                SET revogado = true
                WHERE refresh_token_hash = ?
                  AND revogado = false
                  AND expira_em > ?
                """, hashToken(refreshToken), LocalDateTime.now());
        if (updated == 0) {
            throw new TokenInvalidoException("Sessao nao encontrada para logout");
        }
    }

    private SessaoAutenticada criarSessao(UsuarioLinha usuario) {
        String accessToken = gerarToken();
        String refreshToken = gerarToken();
        jdbcTemplate.update("""
                INSERT INTO sessao_autenticacao (
                    id_sessao_autenticacao, id_usuario, id_escola,
                    refresh_token_hash, access_token_hash, expira_em,
                    access_expira_em, revogado, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID(), usuario.id(), usuario.escolaId(), hashToken(refreshToken),
                hashToken(accessToken), LocalDateTime.now().plusDays(REFRESH_DAYS),
                LocalDateTime.now().plusMinutes(ACCESS_MINUTES), false, LocalDateTime.now());
        return response(accessToken, refreshToken, usuario);
    }

    private SessaoAutenticada response(String accessToken, String refreshToken, UsuarioLinha usuario) {
        return new SessaoAutenticada(
                accessToken,
                refreshToken,
                usuario.id(),
                usuario.username(),
                usuario.nome(),
                listarPerfis(usuario.id()),
                listarPermissoes(usuario.id()));
    }

    private List<String> listarPerfis(UUID usuarioId) {
        return jdbcTemplate.queryForList("""
                SELECT DISTINCT p.codigo
                FROM usuario_perfil up
                JOIN perfil p ON p.id_perfil = up.id_perfil
                WHERE up.id_usuario = ?
                ORDER BY p.codigo
                """, String.class, usuarioId);
    }

    private List<String> listarPermissoes(UUID usuarioId) {
        return jdbcTemplate.queryForList("""
                SELECT DISTINCT pe.codigo
                FROM usuario_perfil up
                JOIN perfil_permissao pp ON pp.id_perfil = up.id_perfil
                JOIN permissao pe ON pe.id_permissao = pp.id_permissao
                WHERE up.id_usuario = ?
                ORDER BY pe.codigo
                """, String.class, usuarioId);
    }

    private UsuarioLinha mapUsuario(ResultSet resultSet, int rowNum) throws SQLException {
        return new UsuarioLinha(
                readUuid(resultSet, "id_usuario"),
                resultSet.getString("username"),
                resultSet.getString("nome"),
                resultSet.getString("senha_hash"),
                readNullableUuid(resultSet, "id_escola"));
    }

    private SessaoLinha mapSessao(ResultSet resultSet, int rowNum) throws SQLException {
        return new SessaoLinha(
                readUuid(resultSet, "id_sessao_autenticacao"),
                mapUsuario(resultSet, rowNum));
    }

    private UUID readUuid(ResultSet resultSet, String column) throws SQLException {
        return UUID.fromString(resultSet.getObject(column).toString());
    }

    private UUID readNullableUuid(ResultSet resultSet, String column) throws SQLException {
        Object value = resultSet.getObject(column);
        return value == null ? null : UUID.fromString(value.toString());
    }

    private CredenciaisInvalidasException credenciaisInvalidas() {
        return new CredenciaisInvalidasException("Usuario ou senha invalidos");
    }

    private String gerarToken() {
        return UUID.randomUUID() + "." + UUID.randomUUID();
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Algoritmo de hash indisponivel", exception);
        }
    }

    private record UsuarioLinha(UUID id, String username, String nome, String senhaHash, UUID escolaId) {
    }

    private record SessaoLinha(UUID sessaoId, UsuarioLinha usuario) {
    }
}
