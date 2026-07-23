package br.com.escola.dashboardqueryservice.infra.persistence.jpa.adapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.escola.dashboardqueryservice.application.port.out.PainelWidgetStorePort;
import br.com.escola.dashboardqueryservice.domain.model.PainelWidget;
import br.com.escola.dashboardqueryservice.infra.persistence.jpa.entity.PainelWidgetJpaEntity;
import br.com.escola.dashboardqueryservice.infra.persistence.jpa.repository.PainelWidgetJpaRepository;

@Component
public class PainelWidgetJpaAdapter implements PainelWidgetStorePort {

    private final PainelWidgetJpaRepository repository;

    public PainelWidgetJpaAdapter(PainelWidgetJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PainelWidget> buscarWidget(UUID escolaId, UUID widgetId) {
        return repository.findByIdAndEscolaId(widgetId, escolaId).map(this::toModel);
    }

    @Override
    public Optional<PainelWidget> buscarWidgetPorCodigo(UUID escolaId, UUID painelId, String codigo) {
        return repository.findByEscolaIdAndPainelIdAndCodigoIgnoreCase(escolaId, painelId, codigo).map(this::toModel);
    }

    @Override
    public Optional<PainelWidget> buscarWidgetPorOrdem(UUID escolaId, UUID painelId, int ordem) {
        return repository.findByEscolaIdAndPainelIdAndOrdem(escolaId, painelId, ordem).map(this::toModel);
    }

    @Override
    public List<PainelWidget> listarWidgets(UUID escolaId, UUID painelId) {
        return repository.findByEscolaIdAndPainelIdOrderByOrdemAsc(escolaId, painelId).stream().map(this::toModel).toList();
    }

    @Override
    public PainelWidget salvarWidget(PainelWidget widget) {
        LocalDateTime now = LocalDateTime.now();
        PainelWidgetJpaEntity entity = repository.findByIdAndEscolaId(widget.id(), widget.escolaId())
                .orElseGet(() -> new PainelWidgetJpaEntity(widget.id(), widget.escolaId(), widget.painelId(),
                        widget.codigo(), widget.titulo(), widget.descricao(), widget.tipoWidget(), widget.ordem(),
                        widget.queryReferencia(), widget.ativo(), now, now));
        entity.atualizar(widget.painelId(), widget.codigo(), widget.titulo(), widget.descricao(), widget.tipoWidget(),
                widget.ordem(), widget.queryReferencia(), widget.ativo(), now);
        return toModel(repository.save(entity));
    }

    @Override
    public void excluirWidget(PainelWidget widget) {
        repository.deleteById(widget.id());
    }

    private PainelWidget toModel(PainelWidgetJpaEntity entity) {
        return new PainelWidget(entity.getId(), entity.getEscolaId(), entity.getPainelId(), entity.getCodigo(),
                entity.getTitulo(), entity.getDescricao(), entity.getTipoWidget(), entity.getOrdem(),
                entity.getQueryReferencia(), entity.isAtivo());
    }
}
