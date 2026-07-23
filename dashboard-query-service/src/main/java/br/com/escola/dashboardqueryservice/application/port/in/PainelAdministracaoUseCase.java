package br.com.escola.dashboardqueryservice.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelConfiguracaoCommand;
import br.com.escola.dashboardqueryservice.application.dto.PainelPublicoCommand;
import br.com.escola.dashboardqueryservice.domain.model.PainelConfiguracao;
import br.com.escola.dashboardqueryservice.domain.model.PainelPublico;

/** Contract prepared for the administrative writes introduced in the next B12 cuts. */
public interface PainelAdministracaoUseCase {

    PainelPublico criarPublico(InternalRequestContext context, PainelPublicoCommand command);

    PainelPublico atualizarPublico(InternalRequestContext context, UUID publicoId, PainelPublicoCommand command);

    List<PainelPublico> listarPublicos(InternalRequestContext context);

    void excluirPublico(InternalRequestContext context, UUID publicoId);

    PainelConfiguracao criarPainel(InternalRequestContext context, PainelConfiguracaoCommand command);

    PainelConfiguracao atualizarPainel(InternalRequestContext context, UUID painelId, PainelConfiguracaoCommand command);

    List<PainelConfiguracao> listarPaineis(InternalRequestContext context, UUID publicoId, String publicoCodigo);

    void excluirPainel(InternalRequestContext context, UUID painelId);
}
