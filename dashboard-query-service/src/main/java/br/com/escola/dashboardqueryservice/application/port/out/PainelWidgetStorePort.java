package br.com.escola.dashboardqueryservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.dashboardqueryservice.domain.model.PainelWidget;

public interface PainelWidgetStorePort {

    Optional<PainelWidget> buscarWidget(UUID escolaId, UUID widgetId);

    Optional<PainelWidget> buscarWidgetPorCodigo(UUID escolaId, UUID painelId, String codigo);

    Optional<PainelWidget> buscarWidgetPorOrdem(UUID escolaId, UUID painelId, int ordem);

    List<PainelWidget> listarWidgets(UUID escolaId, UUID painelId);

    PainelWidget salvarWidget(PainelWidget widget);

    void excluirWidget(PainelWidget widget);
}
