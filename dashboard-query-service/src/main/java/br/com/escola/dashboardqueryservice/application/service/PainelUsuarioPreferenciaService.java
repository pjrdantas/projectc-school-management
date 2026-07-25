package br.com.escola.dashboardqueryservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelUsuarioPreferenciaCommand;
import br.com.escola.dashboardqueryservice.application.dto.PainelUsuarioPreferenciaResponse;
import br.com.escola.dashboardqueryservice.application.exception.PainelQueryServiceResourceNotFoundException;
import br.com.escola.dashboardqueryservice.application.port.in.PainelUsuarioPreferenciaUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.PainelAdministracaoStorePort;
import br.com.escola.dashboardqueryservice.application.port.out.PainelUsuarioPreferenciaStorePort;
import br.com.escola.dashboardqueryservice.application.port.out.PainelWidgetStorePort;
import br.com.escola.dashboardqueryservice.domain.model.PainelConfiguracao;
import br.com.escola.dashboardqueryservice.domain.model.PainelUsuarioPreferencia;
import br.com.escola.dashboardqueryservice.domain.model.PainelWidget;

@Service
@Transactional
public class PainelUsuarioPreferenciaService implements PainelUsuarioPreferenciaUseCase {

    private final PainelUsuarioPreferenciaStorePort store;
    private final PainelWidgetStorePort widgetStore;
    private final PainelAdministracaoStorePort painelStore;
    private final ObjectMapper objectMapper;

    public PainelUsuarioPreferenciaService(
            PainelUsuarioPreferenciaStorePort store,
            PainelWidgetStorePort widgetStore,
            PainelAdministracaoStorePort painelStore,
            ObjectMapper objectMapper) {
        this.store = store;
        this.widgetStore = widgetStore;
        this.painelStore = painelStore;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PainelUsuarioPreferenciaResponse> listar(InternalRequestContext context, UUID usuarioId, UUID painelId) {
        usuarioDoContexto(context, usuarioId);
        return store.listar(context.escolaId(), usuarioId).stream()
                .map(preferencia -> resposta(context.escolaId(), preferencia))
                .filter(resposta -> painelId == null || painelId.equals(resposta.painelId()))
                .toList();
    }

    @Override
    public PainelUsuarioPreferenciaResponse salvar(
            InternalRequestContext context, UUID usuarioId, UUID widgetId, PainelUsuarioPreferenciaCommand command) {
        usuarioDoContexto(context, usuarioId);
        PainelWidget widget = widget(context.escolaId(), widgetId);
        PainelUsuarioPreferencia atual = store.buscar(context.escolaId(), usuarioId, widget.id()).orElse(null);
        PainelUsuarioPreferencia salvo = store.salvar(new PainelUsuarioPreferencia(
                atual == null ? UUID.randomUUID() : atual.id(),
                context.escolaId(), usuarioId, widget.id(), !Boolean.FALSE.equals(command.visivel()), command.ordem(),
                normalizarJson(command.configuracaoJson())));
        return resposta(context.escolaId(), salvo);
    }

    @Override
    public void excluir(InternalRequestContext context, UUID usuarioId, UUID widgetId) {
        usuarioDoContexto(context, usuarioId);
        widget(context.escolaId(), widgetId);
        PainelUsuarioPreferencia preferencia = store.buscar(context.escolaId(), usuarioId, widgetId)
                .orElseThrow(() -> naoEncontrado("Preferencia de usuario nao encontrada para o widget"));
        store.excluir(preferencia);
    }

    private PainelUsuarioPreferenciaResponse resposta(UUID escolaId, PainelUsuarioPreferencia preferencia) {
        PainelWidget widget = widget(escolaId, preferencia.widgetId());
        PainelConfiguracao painel = painelStore.buscarPainel(escolaId, widget.painelId())
                .orElseThrow(() -> naoEncontrado("Painel nao encontrado na escola"));
        return new PainelUsuarioPreferenciaResponse(preferencia.id(), preferencia.usuarioId(), widget.id(),
                widget.codigo(), widget.titulo(), painel.id(), painel.codigo(), preferencia.visivel(),
                preferencia.ordem(), preferencia.configuracaoJson());
    }

    private void usuarioDoContexto(InternalRequestContext context, UUID usuarioId) {
        if (!context.usuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("usuarioId deve corresponder ao contexto autenticado");
        }
    }

    private PainelWidget widget(UUID escolaId, UUID widgetId) {
        return widgetStore.buscarWidget(escolaId, widgetId)
                .orElseThrow(() -> naoEncontrado("Widget nao encontrado na escola"));
    }

    private String normalizarJson(String configuracaoJson) {
        if (configuracaoJson == null || configuracaoJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(objectMapper.readTree(configuracaoJson));
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("configuracaoJson deve conter JSON valido", exception);
        }
    }

    private PainelQueryServiceResourceNotFoundException naoEncontrado(String message) {
        return new PainelQueryServiceResourceNotFoundException(message);
    }
}
