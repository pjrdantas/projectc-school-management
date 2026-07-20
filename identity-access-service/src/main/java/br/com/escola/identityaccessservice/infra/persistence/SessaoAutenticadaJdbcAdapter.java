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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.identityaccessservice.application.context.InternalRequestContext;
import br.com.escola.identityaccessservice.application.dto.AuthContextResponse;
import br.com.escola.identityaccessservice.application.dto.EscolaSessaoResponse;
import br.com.escola.identityaccessservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.identityaccessservice.application.port.out.SessaoAutenticadaPort;

@Component
public class SessaoAutenticadaJdbcAdapter implements SessaoAutenticadaPort {

    private final JdbcTemplate jdbcTemplate;

    public SessaoAutenticadaJdbcAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EscolaSessaoResponse> listarEscolasDisponiveis(
            String accessToken,
            InternalRequestContext context) {
        SessaoLinha sessao = buscarSessao(accessToken);
        List<EscolaSessaoResponse> escolas = jdbcTemplate.query("""
                SELECT e.id_escola, e.nome
                FROM usuario_escola ue
                JOIN escola e ON e.id_escola = ue.id_escola
                WHERE ue.id_usuario = ?
                  AND e.ativo = true
                ORDER BY e.nome, e.id_escola
                """, (resultSet, rowNum) -> new EscolaSessaoResponse(
                        readUuid(resultSet, "id_escola"),
                        resultSet.getString("nome"),
                        readUuid(resultSet, "id_escola").equals(sessao.escolaId())),
                sessao.usuarioId());

        if (!escolas.isEmpty() || sessao.escolaId() == null) {
            return escolas;
        }
        return jdbcTemplate.query("""
                SELECT id_escola, nome
                FROM escola
                WHERE id_escola = ?
                  AND ativo = true
                """, (resultSet, rowNum) -> new EscolaSessaoResponse(
                        readUuid(resultSet, "id_escola"),
                        resultSet.getString("nome"),
                        true),
                sessao.escolaId());
    }

    @Override
    @Transactional
    public AuthContextResponse selecionarEscolaAtiva(
            String accessToken,
            InternalRequestContext context,
            UUID escolaId) {
        SessaoLinha sessao = buscarSessao(accessToken);
        EscolaLinha escola = jdbcTemplate.query("""
                SELECT e.id_escola, e.nome
                FROM usuario_escola ue
                JOIN escola e ON e.id_escola = ue.id_escola
                WHERE ue.id_usuario = ?
                  AND ue.id_escola = ?
                  AND e.ativo = true
                """, this::mapEscola, sessao.usuarioId(), escolaId).stream()
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Vinculo do usuario com a escola nao encontrado"));

        jdbcTemplate.update(
                "UPDATE sessao_autenticacao SET id_escola = ? WHERE id_sessao_autenticacao = ?",
                escola.id(),
                sessao.sessaoId());
        return new AuthContextResponse(
                sessao.usuarioId(),
                escola.id(),
                escola.nome(),
                sessao.username());
    }

    private SessaoLinha buscarSessao(String accessToken) {
        return jdbcTemplate.query("""
                SELECT s.id_sessao_autenticacao, s.id_usuario, s.id_escola, u.username
                FROM sessao_autenticacao s
                JOIN usuario u ON u.id_usuario = s.id_usuario
                WHERE s.access_token_hash = ?
                  AND s.revogado = false
                  AND s.access_expira_em > ?
                """, this::mapSessao, hashToken(accessToken), LocalDateTime.now()).stream()
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sessao autenticada nao encontrada"));
    }

    private SessaoLinha mapSessao(ResultSet resultSet, int rowNum) throws SQLException {
        return new SessaoLinha(
                readUuid(resultSet, "id_sessao_autenticacao"),
                readUuid(resultSet, "id_usuario"),
                readUuid(resultSet, "id_escola"),
                resultSet.getString("username"));
    }

    private EscolaLinha mapEscola(ResultSet resultSet, int rowNum) throws SQLException {
        return new EscolaLinha(readUuid(resultSet, "id_escola"), resultSet.getString("nome"));
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

    private record SessaoLinha(UUID sessaoId, UUID usuarioId, UUID escolaId, String username) {
    }

    private record EscolaLinha(UUID id, String nome) {
    }
}
