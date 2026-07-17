package br.com.escola.peopleservice.application.port.out;

import java.util.List;

import br.com.escola.peopleservice.application.state.LeituraModeloSyncSummary.TableOperationReport;

public interface CatalogoReadModelSyncPort {

    List<TableOperationReport> synchronize(boolean backfillEnabled, boolean reconciliationEnabled, int batchSize);
}


