package br.com.escola.dashboardqueryservice.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelUsuarioPreferenciaCommand;
import br.com.escola.dashboardqueryservice.application.dto.PainelUsuarioPreferenciaResponse;

public interface PainelUsuarioPreferenciaUseCase {

    List<PainelUsuarioPreferenciaResponse> listar(InternalRequestContext context, UUID usuarioId, UUID painelId);

    PainelUsuarioPreferenciaResponse salvar(
            InternalRequestContext context, UUID usuarioId, UUID widgetId, PainelUsuarioPreferenciaCommand command);

    void excluir(InternalRequestContext context, UUID usuarioId, UUID widgetId);
}
