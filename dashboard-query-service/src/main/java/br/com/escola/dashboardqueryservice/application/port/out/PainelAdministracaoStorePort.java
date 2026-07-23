package br.com.escola.dashboardqueryservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.dashboardqueryservice.domain.model.PainelConfiguracao;
import br.com.escola.dashboardqueryservice.domain.model.PainelPublico;

/** Local persistence boundary for dashboard configuration aggregates. */
public interface PainelAdministracaoStorePort {

    Optional<PainelPublico> buscarPublico(UUID escolaId, UUID publicoId);

    Optional<PainelPublico> buscarPublicoPorCodigo(UUID escolaId, String codigo);

    List<PainelPublico> listarPublicos(UUID escolaId);

    PainelPublico salvarPublico(PainelPublico publico);

    boolean existePainelParaPublico(UUID escolaId, UUID publicoId);

    void excluirPublico(PainelPublico publico);

    Optional<PainelConfiguracao> buscarPainel(UUID escolaId, UUID painelId);

    Optional<PainelConfiguracao> buscarPainelPorCodigo(UUID escolaId, String codigo);

    List<PainelConfiguracao> listarPaineis(UUID escolaId, UUID publicoId, String publicoCodigo);

    PainelConfiguracao salvarPainel(PainelConfiguracao painel);

    boolean existeWidgetParaPainel(UUID escolaId, UUID painelId);

    void excluirPainel(PainelConfiguracao painel);
}
