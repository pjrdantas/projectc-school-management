package br.com.escola.catalog.application.port.out;

import br.com.escola.catalog.application.migration.CatalogMigrationSnapshot;

public interface CatalogMigrationTargetPort {

    void aplicar(CatalogMigrationSnapshot snapshot);

    CatalogMigrationSnapshot carregarSnapshot();
}
