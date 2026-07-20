package br.com.escola.dashboardqueryservice.application.port.out;

import br.com.escola.dashboardqueryservice.application.dto.PainelProjecaoUpsertRequest;

public interface PainelProjecaoPort {

    void salvar(java.util.UUID escolaId, PainelProjecaoUpsertRequest request);
}
