package br.com.escola.institutionaltenantservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import br.com.escola.institutionaltenantservice.infra.config.CargaVinculoEscolaProperties;

class CargaVinculoEscolaExecutorTest {

    @Test
    void deveMigrarEReconciliarVinculosDeFormaIdempotente() throws Exception {
        String sourceUrl = url("institutional-link-source");
        String targetUrl = url("institutional-link-target");
        migrate(sourceUrl);
        migrate(targetUrl);
        JdbcTemplate source = jdbc(sourceUrl);
        JdbcTemplate target = jdbc(targetUrl);
        UUID escolaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID vinculoId = UUID.randomUUID();
        inserirEscola(source, escolaId);
        inserirEscola(target, escolaId);
        inserirVinculo(source, vinculoId, usuarioId, escolaId);

        CargaVinculoEscolaProperties properties = properties(sourceUrl, targetUrl);
        CargaVinculoEscolaExecutor executor = new CargaVinculoEscolaExecutor(source, target, 1);
        CargaVinculoEscolaRunner runner = new CargaVinculoEscolaRunner(executor, properties);

        runner.run(null);
        runner.run(null);

        CargaVinculoEscolaReport report = executor.execute();
        assertThat(report.reconciled()).isTrue();
        assertThat(report.sourceRows()).isOne();
        assertThat(report.targetRows()).isOne();
        assertThat(target.queryForObject(
                "SELECT id_usuario FROM usuario_escola WHERE id_usuario_escola = ?",
                UUID.class,
                vinculoId)).isEqualTo(usuarioId);
    }

    @Test
    void deveBloquearCargaQuandoEscolaDoVinculoNaoExisteNoDestino() {
        String sourceUrl = url("institutional-link-missing-source");
        String targetUrl = url("institutional-link-missing-target");
        migrate(sourceUrl);
        migrate(targetUrl);
        JdbcTemplate source = jdbc(sourceUrl);
        JdbcTemplate target = jdbc(targetUrl);
        UUID escolaId = UUID.randomUUID();
        inserirEscola(source, escolaId);
        inserirVinculo(source, UUID.randomUUID(), UUID.randomUUID(), escolaId);

        CargaVinculoEscolaExecutor executor = new CargaVinculoEscolaExecutor(source, target, 10);

        assertThatThrownBy(executor::execute)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(escolaId.toString());
        assertThat(target.queryForObject("SELECT COUNT(1) FROM usuario_escola", Integer.class)).isZero();
    }

    private CargaVinculoEscolaProperties properties(String sourceUrl, String targetUrl) {
        return new CargaVinculoEscolaProperties(
                true, sourceUrl, "sa", "", targetUrl, "sa", "",
                "classpath:db/institutional-tenant/migration", 1, true);
    }

    private void inserirEscola(JdbcTemplate jdbcTemplate, UUID escolaId) {
        jdbcTemplate.update("""
                INSERT INTO escola (id_escola, nome, ativo, created_at)
                VALUES (?, ?, ?, ?)
                """, escolaId, "Escola do vinculo", true, LocalDateTime.of(2026, 7, 20, 17, 20));
    }

    private void inserirVinculo(
            JdbcTemplate jdbcTemplate,
            UUID vinculoId,
            UUID usuarioId,
            UUID escolaId) {
        jdbcTemplate.update("""
                INSERT INTO usuario_escola (id_usuario_escola, id_usuario, id_escola, created_at)
                VALUES (?, ?, ?, ?)
                """, vinculoId, usuarioId, escolaId, LocalDateTime.of(2026, 7, 20, 17, 21));
    }

    private void migrate(String url) {
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/institutional-tenant/migration")
                .load()
                .migrate();
    }

    private JdbcTemplate jdbc(String url) {
        return new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
    }

    private String url(String name) {
        return "jdbc:h2:mem:" + name + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    }
}
