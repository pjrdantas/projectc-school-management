package br.com.escola.catalog.infra.migration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import br.com.escola.catalog.application.migration.MigracaoSnapshot;
import br.com.escola.catalog.application.port.out.MigracaoOrigemPort;

@Component
@ConditionalOnProperty(name = "catalog.migration.enabled", havingValue = "true")
public class JdbcMigracaoOrigemAdapter implements MigracaoOrigemPort {

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;

    public JdbcMigracaoOrigemAdapter(MigracaoOrigemConnection connection) {
        this.jdbc = connection.jdbc();
        this.transaction = connection.transaction();
    }

    @Override
    public MigracaoSnapshot carregarSnapshot() {
        MigracaoSnapshot snapshot = transaction.execute(status -> JdbcSnapshotReader.read(jdbc, true));
        if (snapshot == null) {
            throw new IllegalStateException("Nao foi possivel ler o snapshot do monolito");
        }
        return snapshot;
    }
}

