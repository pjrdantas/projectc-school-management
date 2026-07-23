package br.com.escola.identityaccessservice.infra.persistence;

import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import br.com.escola.identityaccessservice.application.exception.AcessoNegadoException;
import br.com.escola.identityaccessservice.application.port.out.AutorizacaoAdministrativaPort;

@Component
public class AutorizacaoAdministrativaJdbcAdapter implements AutorizacaoAdministrativaPort {

    private final JdbcTemplate jdbcTemplate;

    public AutorizacaoAdministrativaJdbcAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void autorizar(UUID usuarioId, String autoridade) {
        Integer allowed = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM usuario_perfil up
                JOIN perfil p ON p.id_perfil = up.id_perfil
                LEFT JOIN perfil_permissao pp ON pp.id_perfil = p.id_perfil
                LEFT JOIN permissao pe ON pe.id_permissao = pp.id_permissao
                WHERE up.id_usuario = ?
                  AND (p.codigo = 'ADMIN' OR pe.codigo = 'ADMIN' OR pe.codigo = ?)
                """, Integer.class, usuarioId, autoridade);
        if (allowed == null || allowed == 0) {
            throw new AcessoNegadoException("Usuario sem autoridade para administrar acessos");
        }
    }
}
