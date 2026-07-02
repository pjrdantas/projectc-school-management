package br.com.escola.peopleservice.application.port.out;

import java.util.List;

import br.com.escola.peopleservice.application.dto.PeopleLocalPersistenceOperationReport.TableOperationReport;

public interface PeopleCatalogReadModelSyncPort {

    List<TableOperationReport> synchronize(boolean backfillEnabled, boolean reconciliationEnabled, int batchSize);
}
