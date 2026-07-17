package br.com.escola.identityaccessservice.infra.persistence;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import br.com.escola.identityaccessservice.application.dto.AuthContextResponse;
import br.com.escola.identityaccessservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.identityaccessservice.application.port.out.ContextoAutenticadoPort;

@Component
public class ContextoAutenticadoJdbcAdapter implements ContextoAutenticadoPort {

    private static final UUID ESCOLA_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");
    private static final String ESCOLA_PADRAO_NOME = "Escola padrao";

    private final JdbcTemplate jdbcTemplate;

    public ContextoAutenticadoJdbcAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AuthContextResponse consultarContextoAtual(String accessToken) {
        List<ContextoAutenticadoLinha> resultados = jdbcTemplate.query("""
                SELECT
                    s.id_escola AS sessao_escola_id,
                    es.nome AS sessao_escola_nome,
                    u.id_usuario AS usuario_id,
                    u.username AS username,
                    u.id_escola AS usuario_escola_id,
                    eu.nome AS usuario_escola_nome,
                    s.access_expira_em AS access_expira_em
                FROM sessao_autenticacao s
                JOIN usuario u ON u.id_usuario = s.id_usuario
                LEFT JOIN escola es ON es.id_escola = s.id_escola
                LEFT JOIN escola eu ON eu.id_escola = u.id_escola
                WHERE s.access_token_hash = ?
                  AND s.revogado = false
                """, this::mapRow, hashToken(accessToken));

        return resultados.stream()
                .filter(this::sessaoVigente)
                .map(linha -> new AuthContextResponse(
                        linha.usuarioId(),
                        linha.escolaId(),
                        linha.escolaNome(),
                        linha.username()))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Contexto autenticado nao encontrado"));
    }

    private ContextoAutenticadoLinha mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        UUID sessaoEscolaId = readUuid(resultSet, "sessao_escola_id");
        String sessaoEscolaNome = resultSet.getString("sessao_escola_nome");
        UUID usuarioEscolaId = readUuid(resultSet, "usuario_escola_id");
        String usuarioEscolaNome = resultSet.getString("usuario_escola_nome");

        UUID escolaId = sessaoEscolaId != null ? sessaoEscolaId : usuarioEscolaId;
        String escolaNome = sessaoEscolaNome != null ? sessaoEscolaNome : usuarioEscolaNome;
        if (escolaId == null) {
            escolaId = ESCOLA_PADRAO_ID;
            escolaNome = ESCOLA_PADRAO_NOME;
        }

        return new ContextoAutenticadoLinha(
                readUuid(resultSet, "usuario_id"),
                escolaId,
                escolaNome,
                resultSet.getString("username"),
                readTimestamp(resultSet, "access_expira_em"));
    }

    private boolean sessaoVigente(ContextoAutenticadoLinha linha) {
        return linha.accessExpiraEm() != null && linha.accessExpiraEm().isAfter(LocalDateTime.now());
    }

    private UUID readUuid(ResultSet resultSet, String column) throws SQLException {
        Object value = resultSet.getObject(column);
        if (value == null) {
            return null;
        }
        if (value instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(value.toString());
    }

    private LocalDateTime readTimestamp(ResultSet resultSet, String column) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String hashToken(String token) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Algoritmo de hash indisponivel", exception);
        }
    }

    private record ContextoAutenticadoLinha(
            UUID usuarioId,
            UUID escolaId,
            String escolaNome,
            String username,
            LocalDateTime accessExpiraEm) {
    }
}
