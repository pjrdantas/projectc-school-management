package br.com.escola.identityaccessservice.infra.persistence;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.identityaccessservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.identityaccessservice.application.model.ContextoSessaoAutenticada;
import br.com.escola.identityaccessservice.application.port.out.SessaoAutenticadaPort;

@Component
public class SessaoAutenticadaJdbcAdapter implements SessaoAutenticadaPort {

    private static final UUID ESCOLA_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");

    private final JdbcTemplate jdbcTemplate;

    public SessaoAutenticadaJdbcAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public ContextoSessaoAutenticada consultar(String accessToken) {
        return jdbcTemplate.query("""
                SELECT
                    s.id_sessao_autenticacao,
                    s.id_usuario,
                    s.id_escola AS sessao_escola_id,
                    u.id_escola AS usuario_escola_id,
                    u.username
                FROM sessao_autenticacao s
                JOIN usuario u ON u.id_usuario = s.id_usuario
                WHERE s.access_token_hash = ?
                  AND s.revogado = false
                  AND s.access_expira_em > ?
                """, this::mapContexto, hashToken(accessToken), LocalDateTime.now()).stream()
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sessao autenticada nao encontrada"));
    }

    @Override
    @Transactional
    public void atualizarEscolaAtiva(UUID sessaoId, UUID escolaId) {
        int atualizadas = jdbcTemplate.update(
                "UPDATE sessao_autenticacao SET id_escola = ? WHERE id_sessao_autenticacao = ?",
                escolaId,
                sessaoId);
        if (atualizadas != 1) {
            throw new RecursoNaoEncontradoException("Sessao autenticada nao encontrada");
        }
    }

    private ContextoSessaoAutenticada mapContexto(ResultSet resultSet, int rowNum) throws SQLException {
        UUID escolaSessao = readUuid(resultSet, "sessao_escola_id");
        UUID escolaUsuario = readUuid(resultSet, "usuario_escola_id");
        UUID escolaId = escolaSessao != null
                ? escolaSessao
                : escolaUsuario != null ? escolaUsuario : ESCOLA_PADRAO_ID;
        return new ContextoSessaoAutenticada(
                readUuid(resultSet, "id_sessao_autenticacao"),
                readUuid(resultSet, "id_usuario"),
                escolaId,
                resultSet.getString("username"));
    }

    private UUID readUuid(ResultSet resultSet, String column) throws SQLException {
        Object value = resultSet.getObject(column);
        return value == null ? null : UUID.fromString(value.toString());
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
}
