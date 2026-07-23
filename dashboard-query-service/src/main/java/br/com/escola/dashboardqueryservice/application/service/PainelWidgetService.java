package br.com.escola.dashboardqueryservice.application.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelWidgetCommand;
import br.com.escola.dashboardqueryservice.application.exception.PainelQueryServiceConflictException;
import br.com.escola.dashboardqueryservice.application.exception.PainelQueryServiceResourceNotFoundException;
import br.com.escola.dashboardqueryservice.application.port.in.PainelWidgetUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.PainelAdministracaoStorePort;
import br.com.escola.dashboardqueryservice.application.port.out.PainelUsuarioPreferenciaStorePort;
import br.com.escola.dashboardqueryservice.application.port.out.PainelWidgetStorePort;
import br.com.escola.dashboardqueryservice.domain.model.PainelConfiguracao;
import br.com.escola.dashboardqueryservice.domain.model.PainelWidget;

@Service
@Transactional
public class PainelWidgetService implements PainelWidgetUseCase {

    private final PainelAdministracaoStorePort painelStore;
    private final PainelWidgetStorePort widgetStore;
    private final PainelUsuarioPreferenciaStorePort preferenciaStore;

    public PainelWidgetService(
            PainelAdministracaoStorePort painelStore,
            PainelWidgetStorePort widgetStore,
            PainelUsuarioPreferenciaStorePort preferenciaStore) {
        this.painelStore = painelStore;
        this.widgetStore = widgetStore;
        this.preferenciaStore = preferenciaStore;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PainelWidget> listarWidgets(InternalRequestContext context, UUID painelId) {
        painel(context.escolaId(), painelId);
        return widgetStore.listarWidgets(context.escolaId(), painelId);
    }

    @Override
    public PainelWidget criarWidget(InternalRequestContext context, PainelWidgetCommand command) {
        painel(context.escolaId(), command.painelId());
        return salvar(context.escolaId(), UUID.randomUUID(), command, null);
    }

    @Override
    public PainelWidget atualizarWidget(InternalRequestContext context, UUID widgetId, PainelWidgetCommand command) {
        PainelWidget atual = widget(context.escolaId(), widgetId);
        painel(context.escolaId(), command.painelId());
        return salvar(context.escolaId(), atual.id(), command, atual.id());
    }

    @Override
    public void excluirWidget(InternalRequestContext context, UUID widgetId) {
        PainelWidget widget = widget(context.escolaId(), widgetId);
        if (preferenciaStore.existeParaWidget(context.escolaId(), widget.id())) {
            throw new PainelQueryServiceConflictException(
                    "Widget possui preferencias de usuario e nao pode ser removido");
        }
        widgetStore.excluirWidget(widget);
    }

    private PainelWidget salvar(UUID escolaId, UUID widgetId, PainelWidgetCommand command, UUID widgetAtualId) {
        String codigo = codigo(command.codigo());
        validarDisponivel(widgetStore.buscarWidgetPorCodigo(escolaId, command.painelId(), codigo), widgetAtualId,
                "Ja existe widget com este codigo no painel");
        validarDisponivel(widgetStore.buscarWidgetPorOrdem(escolaId, command.painelId(), command.ordem()), widgetAtualId,
                "Ja existe widget nesta ordem no painel");
        return widgetStore.salvarWidget(new PainelWidget(widgetId, escolaId, command.painelId(), codigo,
                texto(command.titulo()), textoOpcional(command.descricao()), codigo(command.tipoWidget()), command.ordem(),
                textoOpcional(command.queryReferencia()), !Boolean.FALSE.equals(command.ativo())));
    }

    private void validarDisponivel(Optional<PainelWidget> existente, UUID widgetAtualId, String mensagem) {
        existente.filter(widget -> !widget.id().equals(widgetAtualId))
                .ifPresent(widget -> { throw new PainelQueryServiceConflictException(mensagem); });
    }

    private PainelConfiguracao painel(UUID escolaId, UUID painelId) {
        return painelStore.buscarPainel(escolaId, painelId)
                .orElseThrow(() -> new PainelQueryServiceResourceNotFoundException("Painel nao encontrado na escola"));
    }

    private PainelWidget widget(UUID escolaId, UUID widgetId) {
        return widgetStore.buscarWidget(escolaId, widgetId)
                .orElseThrow(() -> new PainelQueryServiceResourceNotFoundException("Widget nao encontrado na escola"));
    }

    private String codigo(String value) {
        return texto(value).toUpperCase(Locale.ROOT);
    }

    private String texto(String value) {
        return value.trim();
    }

    private String textoOpcional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
