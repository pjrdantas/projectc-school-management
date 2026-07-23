package br.com.escola.dashboardqueryservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class PainelBackfillExecutorTest {

    @Test
    void deveImportarEstadoDePainelComEscolaExplicitaEReconciliarDeFormaIdempotente() {
        UUID escolaId = UUID.randomUUID();
        UUID publicoId = UUID.randomUUID();
        UUID painelId = UUID.randomUUID();
        UUID widgetId = UUID.randomUUID();
        UUID preferenciaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID snapshotId = UUID.randomUUID();
        JdbcTemplate source = jdbc("dashboard-backfill-source");
        JdbcTemplate target = jdbc("dashboard-backfill-target");
        criarFonte(source);
        criarDestino(target);
        source.update("INSERT INTO escola (id_escola, nome) VALUES (?, ?)", escolaId, "Escola Backfill");
        source.update("INSERT INTO publico_dashboard VALUES (?, ?, ?)", publicoId, "DIRETOR", "Diretoria");
        source.update("INSERT INTO dashboard VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)", painelId, publicoId, "GERAL", "Visao geral", "Resumo", true);
        source.update("INSERT INTO dashboard_widget VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)", widgetId, painelId, "MATRICULAS", "Matriculas", null, "INDICADOR", 0, null, true);
        source.update("INSERT INTO dashboard_usuario_configuracao VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", preferenciaId, usuarioId, widgetId, true, 0, "{\"colunas\":2}");
        source.update("INSERT INTO dashboard_indicador_snapshot VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)", snapshotId, publicoId, escolaId, "TOTAL_MATRICULAS", "Total", 12, "12", java.sql.Date.valueOf("2026-07-23"));

        PainelBackfillExecutor executor = new PainelBackfillExecutor(source, target, escolaId, 1);
        PainelBackfillReport first = executor.execute();
        PainelBackfillReport second = executor.execute();

        assertThat(first.reconciled()).isTrue();
        assertThat(second.reconciled()).isTrue();
        assertThat(first.sourceRows()).containsEntry("painel_indicador_snapshot", 1);
        assertThat(target.queryForObject("SELECT COUNT(*) FROM painel_publico WHERE escola_id=?", Integer.class, escolaId)).isEqualTo(1);
        assertThat(target.queryForObject("SELECT COUNT(*) FROM painel_configuracao WHERE escola_id=?", Integer.class, escolaId)).isEqualTo(1);
        assertThat(target.queryForObject("SELECT COUNT(*) FROM painel_widget WHERE escola_id=?", Integer.class, escolaId)).isEqualTo(1);
        assertThat(target.queryForObject("SELECT COUNT(*) FROM painel_usuario_preferencia WHERE escola_id=?", Integer.class, escolaId)).isEqualTo(1);
        assertThat(target.queryForObject("SELECT COUNT(*) FROM painel_indicador_snapshot WHERE escola_id=?", Integer.class, escolaId)).isEqualTo(1);
    }

    private void criarFonte(JdbcTemplate jdbc) {
        jdbc.execute("CREATE TABLE escola (id_escola UUID PRIMARY KEY, nome VARCHAR(255))");
        jdbc.execute("CREATE TABLE publico_dashboard (id_publico_dashboard UUID PRIMARY KEY, codigo VARCHAR(40), descricao VARCHAR(120))");
        jdbc.execute("CREATE TABLE dashboard (id_dashboard UUID PRIMARY KEY, id_publico_dashboard UUID, codigo VARCHAR(80), nome VARCHAR(150), descricao VARCHAR(255), ativo BOOLEAN, created_at TIMESTAMP)");
        jdbc.execute("CREATE TABLE dashboard_widget (id_dashboard_widget UUID PRIMARY KEY, id_dashboard UUID, codigo VARCHAR(80), titulo VARCHAR(150), descricao VARCHAR(255), tipo_widget VARCHAR(40), ordem INTEGER, query_referencia VARCHAR(150), ativo BOOLEAN, created_at TIMESTAMP)");
        jdbc.execute("CREATE TABLE dashboard_usuario_configuracao (id_dashboard_usuario_configuracao UUID PRIMARY KEY, id_usuario UUID, id_dashboard_widget UUID, visivel BOOLEAN, ordem INTEGER, configuracao_json VARCHAR(4000), created_at TIMESTAMP, updated_at TIMESTAMP)");
        jdbc.execute("CREATE TABLE dashboard_indicador_snapshot (id_dashboard_indicador_snapshot UUID PRIMARY KEY, id_publico_dashboard UUID, id_escola UUID, codigo_indicador VARCHAR(100), descricao VARCHAR(255), valor_numeric DECIMAL(19,4), valor_texto VARCHAR(255), referencia_data DATE, created_at TIMESTAMP)");
    }

    private void criarDestino(JdbcTemplate jdbc) {
        jdbc.execute("CREATE TABLE painel_publico (id UUID PRIMARY KEY, escola_id UUID, codigo VARCHAR(40), descricao VARCHAR(120), created_at TIMESTAMP, updated_at TIMESTAMP)");
        jdbc.execute("CREATE TABLE painel_configuracao (id UUID PRIMARY KEY, escola_id UUID, publico_id UUID, codigo VARCHAR(80), nome VARCHAR(150), descricao VARCHAR(255), ativo BOOLEAN, created_at TIMESTAMP, updated_at TIMESTAMP)");
        jdbc.execute("CREATE TABLE painel_widget (id UUID PRIMARY KEY, escola_id UUID, painel_id UUID, codigo VARCHAR(80), titulo VARCHAR(150), descricao VARCHAR(255), tipo_widget VARCHAR(40), ordem INTEGER, query_referencia VARCHAR(150), ativo BOOLEAN, created_at TIMESTAMP, updated_at TIMESTAMP)");
        jdbc.execute("CREATE TABLE painel_usuario_preferencia (id UUID PRIMARY KEY, escola_id UUID, usuario_id UUID, widget_id UUID, visivel BOOLEAN, ordem INTEGER, configuracao_json VARCHAR(4000), created_at TIMESTAMP, updated_at TIMESTAMP)");
        jdbc.execute("CREATE TABLE painel_indicador_snapshot (id UUID PRIMARY KEY, escola_id UUID, publico_id UUID, codigo_indicador VARCHAR(100), descricao VARCHAR(255), valor_numeric DECIMAL(19,4), valor_texto VARCHAR(255), escola_nome VARCHAR(255), referencia_data DATE, created_at TIMESTAMP, updated_at TIMESTAMP)");
    }

    private JdbcTemplate jdbc(String name) {
        return new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:" + name + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", ""));
    }
}
