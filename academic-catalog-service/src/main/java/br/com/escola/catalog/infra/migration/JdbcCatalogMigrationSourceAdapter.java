package br.com.escola.catalog.infra.migration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import br.com.escola.catalog.application.migration.CatalogMigrationSnapshot;
import br.com.escola.catalog.application.port.out.CatalogMigrationSourcePort;

@Component
@ConditionalOnProperty(name = "catalog.migration.enabled", havingValue = "true")
public class JdbcCatalogMigrationSourceAdapter implements CatalogMigrationSourcePort {

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;

    public JdbcCatalogMigrationSourceAdapter(CatalogMigrationSourceConnection connection) {
        this.jdbc = connection.jdbc();
        this.transaction = connection.transaction();
    }

    @Override
    public CatalogMigrationSnapshot carregarSnapshot() {
        CatalogMigrationSnapshot snapshot = transaction.execute(status -> JdbcCatalogSnapshotReader.read(jdbc, true));
        if (snapshot == null) {
            throw new IllegalStateException("Nao foi possivel ler o snapshot do monolito");
        }
        return snapshot;
    }
}
