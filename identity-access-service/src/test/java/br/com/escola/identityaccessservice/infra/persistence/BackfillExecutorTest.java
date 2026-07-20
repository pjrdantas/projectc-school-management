package br.com.escola.identityaccessservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class BackfillExecutorTest {

    @Test
    void deveCopiarEReconciliarAsSeisTabelasDeFormaIdempotente() {
        JdbcTemplate source = database("identity-backfill-source");
        JdbcTemplate target = database("identity-backfill-target");
        UUID permissaoId = UUID.randomUUID();
        UUID perfilId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID sessaoId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 7, 20, 16, 30);

        source.update("""
                INSERT INTO permissao (id_permissao, codigo, descricao, created_at)
                VALUES (?, ?, ?, ?)
                """, permissaoId, "ADMIN", "Administracao", now);
        source.update("""
                INSERT INTO perfil (id_perfil, codigo, nome, descricao, created_at)
                VALUES (?, ?, ?, ?, ?)
                """, perfilId, "ADMIN", "Administrador", "Acesso total", now);
        source.update("""
                INSERT INTO perfil_permissao (id_perfil_permissao, id_perfil, id_permissao)
                VALUES (?, ?, ?)
                """, UUID.randomUUID(), perfilId, permissaoId);
        source.update("""
                INSERT INTO usuario (
                    id_usuario, username, nome, email, senha_hash, ativo, id_escola, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, usuarioId, "admin", "Administrador", "admin@escola.com",
                "$2a$10$hash-preservado", true, escolaId, now);
        source.update("""
                INSERT INTO usuario_perfil (id_usuario_perfil, id_usuario, id_perfil)
                VALUES (?, ?, ?)
                """, UUID.randomUUID(), usuarioId, perfilId);
        source.update("""
                INSERT INTO sessao_autenticacao (
                    id_sessao_autenticacao, id_usuario, id_escola, refresh_token_hash,
                    access_token_hash, expira_em, access_expira_em, revogado, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, sessaoId, usuarioId, escolaId, "refresh-hash", "access-hash",
                now.plusDays(7), now.plusMinutes(30), false, now);

        BackfillExecutor executor = new BackfillExecutor(source, target, 2);
        BackfillReport first = executor.execute();
        BackfillReport second = executor.execute();

        assertThat(first.reconciled()).isTrue();
        assertThat(second.reconciled()).isTrue();
        assertThat(first.copiedRows()).containsEntry("usuario", 1).containsEntry("sessao_autenticacao", 1);
        assertThat(second.sourceRows()).isEqualTo(second.targetRows());
        assertThat(target.queryForObject(
                "SELECT senha_hash FROM usuario WHERE id_usuario = ?", String.class, usuarioId))
                .isEqualTo("$2a$10$hash-preservado");
        assertThat(target.queryForObject(
                "SELECT id_perfil FROM usuario_perfil WHERE id_usuario = ?", UUID.class, usuarioId))
                .isEqualTo(perfilId);
        assertThat(target.queryForObject(
                "SELECT revogado FROM sessao_autenticacao WHERE id_sessao_autenticacao = ?",
                Boolean.class,
                sessaoId)).isFalse();
    }

    private JdbcTemplate database(String name) {
        String url = "jdbc:h2:mem:" + name + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/identity-access/migration")
                .load()
                .migrate();
        return new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
    }
}
