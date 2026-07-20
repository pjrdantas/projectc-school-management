package br.com.escola.institutionaltenantservice.infra.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import br.com.escola.institutionaltenantservice.application.context.InternalRequestContext;
import br.com.escola.institutionaltenantservice.application.dto.TenantEscolaResponse;
import br.com.escola.institutionaltenantservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.institutionaltenantservice.application.port.out.TenantSessaoPort;

@Component
public class TenantSessaoJdbcAdapter implements TenantSessaoPort {

    private final JdbcTemplate jdbcTemplate;

    public TenantSessaoJdbcAdapter(
            @Qualifier("tenantReadJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<TenantEscolaResponse> listarEscolasDisponiveis(
            String authorization,
            InternalRequestContext context) {
        List<TenantEscolaResponse> escolas = jdbcTemplate.query("""
                SELECT
                    e.id_escola,
                    e.nome
                FROM usuario_escola ue
                JOIN escola e ON e.id_escola = ue.id_escola
                WHERE ue.id_usuario = ?
                ORDER BY
                    CASE WHEN e.id_escola = ? THEN 0 ELSE 1 END,
                    e.nome
                """, (resultSet, rowNum) -> new TenantEscolaResponse(
                        readUuid(resultSet.getObject("id_escola")),
                        resultSet.getString("nome"),
                        context.escolaId() != null
                                && context.escolaId().equals(readUuid(resultSet.getObject("id_escola")))),
                context.usuarioId(),
                context.escolaId());

        if (!escolas.isEmpty()) {
            return escolas;
        }

        if (context.escolaId() == null) {
            return List.of();
        }

        return jdbcTemplate.query("""
                SELECT
                    e.id_escola,
                    e.nome
                FROM escola e
                WHERE e.id_escola = ?
                """, (resultSet, rowNum) -> new TenantEscolaResponse(
                        readUuid(resultSet.getObject("id_escola")),
                        resultSet.getString("nome"),
                        true), context.escolaId());
    }

    private UUID readUuid(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(value.toString());
    }
}
