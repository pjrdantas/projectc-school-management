package br.com.escola.institutionaltenantservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import br.com.escola.institutionaltenantservice.infra.config.CargaEscolaProperties;

class CargaEscolaExecutorTest {

    @Test
    void deveMigrarEReconciliarEscolasDeFormaIdempotente() throws Exception {
        String sourceUrl = url("institutional-school-source");
        String targetUrl = url("institutional-school-target");
        migrate(sourceUrl);
        migrate(targetUrl);
        JdbcTemplate source = jdbc(sourceUrl);
        JdbcTemplate target = jdbc(targetUrl);
        UUID escolaId = UUID.randomUUID();
        UUID enderecoId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 20, 17, 10);
        LocalDateTime updatedAt = createdAt.plusHours(1);
        source.update("""
                INSERT INTO escola (
                    id_escola, nome, codigo_inep, cnpj, telefone, email,
                    id_endereco, ativo, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, escolaId, "Escola Integrada", "INEP-001", "12.345.678/0001-90",
                "11999999999", "contato@escola.com", enderecoId, true, createdAt, updatedAt);

        CargaEscolaProperties properties = new CargaEscolaProperties(
                true, sourceUrl, "sa", "", 1, true);
        CargaEscolaExecutor executor = new CargaEscolaExecutor(source, target, 1);
        CargaEscolaRunner runner = new CargaEscolaRunner(executor, properties);

        runner.run(null);
        runner.run(null);

        CargaEscolaReport report = executor.execute();
        assertThat(report.reconciled()).isTrue();
        assertThat(report.sourceRows()).isOne();
        assertThat(report.targetRows()).isOne();
        assertThat(target.queryForObject(
                "SELECT id_endereco FROM escola WHERE id_escola = ?", UUID.class, escolaId))
                .isEqualTo(enderecoId);
        assertThat(target.queryForObject(
                "SELECT codigo_inep FROM escola WHERE id_escola = ?", String.class, escolaId))
                .isEqualTo("INEP-001");
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
