package br.com.escola.dashboardqueryservice.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelWidgetCommand;
import br.com.escola.dashboardqueryservice.domain.model.PainelWidget;

public interface PainelWidgetUseCase {

    List<PainelWidget> listarWidgets(InternalRequestContext context, UUID painelId);

    PainelWidget criarWidget(InternalRequestContext context, PainelWidgetCommand command);

    PainelWidget atualizarWidget(InternalRequestContext context, UUID widgetId, PainelWidgetCommand command);

    void excluirWidget(InternalRequestContext context, UUID widgetId);
}
