package br.com.escola.institutionaltenantservice.infra.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.institutionaltenantservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.institutionaltenantservice.application.model.ResultadoVinculoUsuarioEscola;
import br.com.escola.institutionaltenantservice.application.model.VinculoUsuarioEscola;
import br.com.escola.institutionaltenantservice.application.port.out.VinculoUsuarioEscolaPort;

@Component
public class VinculoUsuarioEscolaJdbcAdapter implements VinculoUsuarioEscolaPort {

    private static final String SELECT_COLUMNS = """
            SELECT id_usuario_escola, id_usuario, id_escola, created_at
            FROM usuario_escola
            """;

    private final JdbcTemplate jdbcTemplate;

    public VinculoUsuarioEscolaJdbcAdapter(
            @Qualifier("tenantReadJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<VinculoUsuarioEscola> listar(UUID usuarioId, UUID escolaId) {
        StringBuilder sql = new StringBuilder(SELECT_COLUMNS).append(" WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();
        if (usuarioId != null) {
            sql.append(" AND id_usuario = ?");
            parameters.add(usuarioId);
        }
        if (escolaId != null) {
            sql.append(" AND id_escola = ?");
            parameters.add(escolaId);
        }
        sql.append(" ORDER BY created_at, id_usuario_escola");
        return jdbcTemplate.query(sql.toString(), this::map, parameters.toArray());
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public VinculoUsuarioEscola buscar(UUID id) {
        return jdbcTemplate.query(
                SELECT_COLUMNS + " WHERE id_usuario_escola = ?",
                this::map,
                id).stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vinculo usuario-escola nao encontrado"));
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public boolean escolaExiste(UUID escolaId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM escola WHERE id_escola = ?", Integer.class, escolaId);
        return count != null && count > 0;
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public ResultadoVinculoUsuarioEscola garantir(VinculoUsuarioEscola vinculo) {
        List<UUID> escolaBloqueada = jdbcTemplate.query(
                "SELECT id_escola FROM escola WHERE id_escola = ? FOR UPDATE",
                (resultSet, rowNum) -> readUuid(resultSet.getObject("id_escola")),
                vinculo.escolaId());
        if (escolaBloqueada.isEmpty()) {
            throw new RecursoNaoEncontradoException("Escola nao encontrada");
        }
        List<VinculoUsuarioEscola> existente = buscarPorUsuarioEEscola(
                vinculo.usuarioId(), vinculo.escolaId());
        if (!existente.isEmpty()) {
            return new ResultadoVinculoUsuarioEscola(existente.getFirst(), false);
        }
        jdbcTemplate.update("""
                INSERT INTO usuario_escola (
                    id_usuario_escola, id_usuario, id_escola, created_at
                ) VALUES (?, ?, ?, ?)
                """, vinculo.id(), vinculo.usuarioId(), vinculo.escolaId(), vinculo.createdAt());
        return new ResultadoVinculoUsuarioEscola(buscar(vinculo.id()), true);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public void excluir(UUID id) {
        if (jdbcTemplate.update("DELETE FROM usuario_escola WHERE id_usuario_escola = ?", id) == 0) {
            throw new RecursoNaoEncontradoException("Vinculo usuario-escola nao encontrado");
        }
    }

    private List<VinculoUsuarioEscola> buscarPorUsuarioEEscola(UUID usuarioId, UUID escolaId) {
        return jdbcTemplate.query(
                SELECT_COLUMNS + " WHERE id_usuario = ? AND id_escola = ?",
                this::map,
                usuarioId,
                escolaId);
    }

    private VinculoUsuarioEscola map(ResultSet resultSet, int rowNum) throws SQLException {
        return new VinculoUsuarioEscola(
                readUuid(resultSet.getObject("id_usuario_escola")),
                readUuid(resultSet.getObject("id_usuario")),
                readUuid(resultSet.getObject("id_escola")),
                resultSet.getTimestamp("created_at").toLocalDateTime());
    }

    private UUID readUuid(Object value) {
        return UUID.fromString(value.toString());
    }
}
