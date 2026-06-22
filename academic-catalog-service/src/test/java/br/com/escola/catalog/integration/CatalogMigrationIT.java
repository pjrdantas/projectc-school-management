package br.com.escola.catalog.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import br.com.escola.catalog.application.service.CatalogMigrationService;

@Testcontainers
@SpringBootTest(properties = {
        "catalog.migration.enabled=true",
        "catalog.migration.runner-enabled=false",
        "catalog.cache.enabled=false",
        "management.health.redis.enabled=false"
})
class CatalogMigrationIT {

    private static final UUID ESCOLA_A = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID DISCIPLINA_A = UUID.fromString("10000000-0000-0000-0000-000000000003");

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> TARGET = new PostgreSQLContainer<>("postgres:17-alpine");

    @Container
    static final PostgreSQLContainer<?> SOURCE = new PostgreSQLContainer<>("postgres:17-alpine")
            .withInitScript("catalog-migration-source.sql");

    @DynamicPropertySource
    static void migrationProperties(DynamicPropertyRegistry registry) {
        registry.add("catalog.migration.source.url", SOURCE::getJdbcUrl);
        registry.add("catalog.migration.source.username", SOURCE::getUsername);
        registry.add("catalog.migration.source.password", SOURCE::getPassword);
    }

    @Autowired
    private CatalogMigrationService service;

    @Autowired
    private JdbcTemplate targetJdbc;

    @Test
    void deveMigrarRepetirReconciliarPorEscolaEDetectarDivergencia() {
        var first = service.executar(true);

        assertThat(first.applied()).isTrue();
        assertThat(first.reconciled()).isTrue();
        assertThat(first.sourceIssues()).isEmpty();
        assertThat(first.tables())
                .filteredOn(table -> table.table().equals("disciplina") && ESCOLA_A.equals(table.escolaId()))
                .singleElement().satisfies(table -> {
                assertThat(table.sourceCount()).isEqualTo(1);
                assertThat(table.targetCount()).isEqualTo(1);
                assertThat(table.reconciled()).isTrue();
        });

        var second = service.executar(true);
        assertThat(second.reconciled()).isTrue();
        assertThat(targetJdbc.queryForObject("SELECT count(*) FROM disciplina", Integer.class)).isEqualTo(2);

        targetJdbc.update("UPDATE disciplina SET nome = 'Divergente' WHERE id_disciplina = ?", DISCIPLINA_A);
        var dryRun = service.executar(false);

        assertThat(dryRun.applied()).isFalse();
        assertThat(dryRun.reconciled()).isFalse();
        assertThat(dryRun.tables())
                .filteredOn(table -> table.table().equals("disciplina") && ESCOLA_A.equals(table.escolaId()))
                .singleElement().satisfies(table ->
                        assertThat(table.divergentIds()).containsExactly(DISCIPLINA_A));
    }
}
