package br.com.escola.responsiblesservice.application.port.out;

import java.util.List;

import br.com.escola.responsiblesservice.application.state.ResponsiblesReadModelSyncSummary.TableOperationReport;

public interface ResponsiblesReadModelSyncPort {

    List<TableOperationReport> synchronize(boolean backfillEnabled, int batchSize);
}
