package br.com.escola.professorservice.infra.migration;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import br.com.escola.professorservice.application.migration.MigracaoSnapshot;
import br.com.escola.professorservice.application.port.out.MigracaoOrigemPort;

@Component
@ConditionalOnProperty(name = "professor.shadow.migration.enabled", havingValue = "true")
public class JdbcMigracaoOrigemAdapter implements MigracaoOrigemPort {

    private final JdbcTemplate jdbc;
    private final MigracaoOrigemConnection connection;

    public JdbcMigracaoOrigemAdapter(MigracaoOrigemConnection connection) {
        this.jdbc = connection.jdbc();
        this.connection = connection;
    }

    @Override
    public MigracaoSnapshot carregarSnapshot() {
        MigracaoSnapshot snapshot = connection.transaction().execute(status ->
                new MigracaoSnapshot(jdbc.query("""
                        SELECT
                            p.id_professor,
                            p.id_pessoa,
                            pessoa.id_escola,
                            escola.nome AS escola_nome,
                            pessoa.nome_completo,
                            p.registro_profissional,
                            p.formacao,
                            p.ativo,
                            p.created_at,
                            p.updated_at,
                            p.id_usuario
                        FROM professor p
                        JOIN pessoa pessoa ON pessoa.id_pessoa = p.id_pessoa
                        JOIN escola escola ON escola.id_escola = pessoa.id_escola
                        ORDER BY pessoa.id_escola, pessoa.nome_completo, p.id_professor
                        """, this::mapRow)));
        if (snapshot == null) {
            throw new IllegalStateException("Nao foi possivel ler o snapshot de professores do monolito");
        }
        return snapshot;
    }

    private MigracaoSnapshot.CadastroRow mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new MigracaoSnapshot.CadastroRow(
                rs.getObject("id_professor", java.util.UUID.class),
                rs.getObject("id_pessoa", java.util.UUID.class),
                rs.getObject("id_escola", java.util.UUID.class),
                rs.getString("escola_nome"),
                rs.getString("nome_completo"),
                rs.getString("registro_profissional"),
                rs.getString("formacao"),
                rs.getBoolean("ativo"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toLocalDateTime(),
                rs.getObject("id_usuario", java.util.UUID.class));
    }
}


