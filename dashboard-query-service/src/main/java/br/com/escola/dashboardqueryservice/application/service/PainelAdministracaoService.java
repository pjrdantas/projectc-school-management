package br.com.escola.dashboardqueryservice.application.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelConfiguracaoCommand;
import br.com.escola.dashboardqueryservice.application.dto.PainelPublicoCommand;
import br.com.escola.dashboardqueryservice.application.exception.PainelQueryServiceConflictException;
import br.com.escola.dashboardqueryservice.application.exception.PainelQueryServiceResourceNotFoundException;
import br.com.escola.dashboardqueryservice.application.port.in.PainelAdministracaoUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.PainelAdministracaoStorePort;
import br.com.escola.dashboardqueryservice.domain.model.PainelConfiguracao;
import br.com.escola.dashboardqueryservice.domain.model.PainelPublico;

@Service
@Transactional
public class PainelAdministracaoService implements PainelAdministracaoUseCase {

    private final PainelAdministracaoStorePort store;

    public PainelAdministracaoService(PainelAdministracaoStorePort store) {
        this.store = store;
    }

    @Override
    public PainelPublico criarPublico(InternalRequestContext context, PainelPublicoCommand command) {
        String codigo = codigo(command.codigo());
        if (store.buscarPublicoPorCodigo(context.escolaId(), codigo).isPresent()) {
            throw conflito("Ja existe publico com este codigo na escola");
        }
        return store.salvarPublico(new PainelPublico(UUID.randomUUID(), context.escolaId(), codigo, texto(command.descricao())));
    }

    @Override
    public PainelPublico atualizarPublico(InternalRequestContext context, UUID publicoId, PainelPublicoCommand command) {
        PainelPublico atual = publico(context.escolaId(), publicoId);
        String codigo = codigo(command.codigo());
        store.buscarPublicoPorCodigo(context.escolaId(), codigo)
                .filter(existente -> !existente.id().equals(atual.id()))
                .ifPresent(existente -> { throw conflito("Ja existe publico com este codigo na escola"); });
        return store.salvarPublico(new PainelPublico(atual.id(), atual.escolaId(), codigo, texto(command.descricao())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PainelPublico> listarPublicos(InternalRequestContext context) {
        return store.listarPublicos(context.escolaId());
    }

    @Override
    public void excluirPublico(InternalRequestContext context, UUID publicoId) {
        PainelPublico publico = publico(context.escolaId(), publicoId);
        if (store.existePainelParaPublico(context.escolaId(), publico.id())) {
            throw conflito("Publico possui paineis vinculados e nao pode ser removido");
        }
        store.excluirPublico(publico);
    }

    @Override
    public PainelConfiguracao criarPainel(InternalRequestContext context, PainelConfiguracaoCommand command) {
        validarPublico(context.escolaId(), command.publicoId());
        String codigo = codigo(command.codigo());
        if (store.buscarPainelPorCodigo(context.escolaId(), codigo).isPresent()) {
            throw conflito("Ja existe painel com este codigo na escola");
        }
        return salvarPainel(context.escolaId(), UUID.randomUUID(), command, codigo);
    }

    @Override
    public PainelConfiguracao atualizarPainel(InternalRequestContext context, UUID painelId, PainelConfiguracaoCommand command) {
        PainelConfiguracao atual = painel(context.escolaId(), painelId);
        validarPublico(context.escolaId(), command.publicoId());
        String codigo = codigo(command.codigo());
        store.buscarPainelPorCodigo(context.escolaId(), codigo)
                .filter(existente -> !existente.id().equals(atual.id()))
                .ifPresent(existente -> { throw conflito("Ja existe painel com este codigo na escola"); });
        return salvarPainel(context.escolaId(), atual.id(), command, codigo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PainelConfiguracao> listarPaineis(InternalRequestContext context, UUID publicoId, String publicoCodigo) {
        if (publicoId != null) {
            validarPublico(context.escolaId(), publicoId);
        }
        if (publicoCodigo != null && !publicoCodigo.isBlank()) {
            publicoId = store.buscarPublicoPorCodigo(context.escolaId(), codigo(publicoCodigo))
                    .map(PainelPublico::id)
                    .orElseThrow(() -> naoEncontrado("Publico nao encontrado na escola"));
        }
        return store.listarPaineis(context.escolaId(), publicoId, publicoCodigo);
    }

    @Override
    public void excluirPainel(InternalRequestContext context, UUID painelId) {
        PainelConfiguracao painel = painel(context.escolaId(), painelId);
        if (store.existeWidgetParaPainel(context.escolaId(), painel.id())) {
            throw conflito("Painel possui widgets vinculados e nao pode ser removido");
        }
        store.excluirPainel(painel);
    }

    private PainelConfiguracao salvarPainel(
            UUID escolaId, UUID painelId, PainelConfiguracaoCommand command, String codigo) {
        return store.salvarPainel(new PainelConfiguracao(painelId, escolaId, command.publicoId(), codigo,
                texto(command.nome()), textoOpcional(command.descricao()), !Boolean.FALSE.equals(command.ativo())));
    }

    private PainelPublico publico(UUID escolaId, UUID publicoId) {
        return store.buscarPublico(escolaId, publicoId)
                .orElseThrow(() -> naoEncontrado("Publico nao encontrado na escola"));
    }

    private PainelConfiguracao painel(UUID escolaId, UUID painelId) {
        return store.buscarPainel(escolaId, painelId)
                .orElseThrow(() -> naoEncontrado("Painel nao encontrado na escola"));
    }

    private void validarPublico(UUID escolaId, UUID publicoId) {
        publico(escolaId, publicoId);
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

    private PainelQueryServiceConflictException conflito(String message) {
        return new PainelQueryServiceConflictException(message);
    }

    private PainelQueryServiceResourceNotFoundException naoEncontrado(String message) {
        return new PainelQueryServiceResourceNotFoundException(message);
    }
}
