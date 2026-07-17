package br.com.escola.responsiblesservice.application.port.out;

import java.util.List;

import br.com.escola.responsiblesservice.application.state.LeituraModeloSyncSummary.TableOperationReport;

public interface LeituraModeloSyncPort {

    List<TableOperationReport> synchronize(boolean backfillEnabled, boolean reconciliationEnabled, int batchSize);
}

