package br.com.escola.dashboardqueryservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.dashboardqueryservice.domain.model.PainelUsuarioPreferencia;

public interface PainelUsuarioPreferenciaStorePort {

    List<PainelUsuarioPreferencia> listar(UUID escolaId, UUID usuarioId);

    Optional<PainelUsuarioPreferencia> buscar(UUID escolaId, UUID usuarioId, UUID widgetId);

    boolean existeParaWidget(UUID escolaId, UUID widgetId);

    PainelUsuarioPreferencia salvar(PainelUsuarioPreferencia preferencia);

    void excluir(PainelUsuarioPreferencia preferencia);
}
