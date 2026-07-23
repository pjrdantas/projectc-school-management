package br.com.escola.dashboardqueryservice.infra.persistence.jpa.adapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.escola.dashboardqueryservice.application.port.out.PainelUsuarioPreferenciaStorePort;
import br.com.escola.dashboardqueryservice.domain.model.PainelUsuarioPreferencia;
import br.com.escola.dashboardqueryservice.infra.persistence.jpa.entity.PainelUsuarioPreferenciaJpaEntity;
import br.com.escola.dashboardqueryservice.infra.persistence.jpa.repository.PainelUsuarioPreferenciaJpaRepository;

@Component
public class PainelUsuarioPreferenciaJpaAdapter implements PainelUsuarioPreferenciaStorePort {

    private final PainelUsuarioPreferenciaJpaRepository repository;

    public PainelUsuarioPreferenciaJpaAdapter(PainelUsuarioPreferenciaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PainelUsuarioPreferencia> listar(UUID escolaId, UUID usuarioId) {
        return repository.findByEscolaIdAndUsuarioIdOrderByUpdatedAtDesc(escolaId, usuarioId).stream()
                .map(this::toModel).toList();
    }

    @Override
    public Optional<PainelUsuarioPreferencia> buscar(UUID escolaId, UUID usuarioId, UUID widgetId) {
        return repository.findByEscolaIdAndUsuarioIdAndWidgetId(escolaId, usuarioId, widgetId).map(this::toModel);
    }

    @Override
    public boolean existeParaWidget(UUID escolaId, UUID widgetId) {
        return repository.existsByEscolaIdAndWidgetId(escolaId, widgetId);
    }

    @Override
    public PainelUsuarioPreferencia salvar(PainelUsuarioPreferencia preferencia) {
        LocalDateTime now = LocalDateTime.now();
        PainelUsuarioPreferenciaJpaEntity entity = repository
                .findByEscolaIdAndUsuarioIdAndWidgetId(preferencia.escolaId(), preferencia.usuarioId(), preferencia.widgetId())
                .orElseGet(() -> new PainelUsuarioPreferenciaJpaEntity(preferencia.id(), preferencia.escolaId(),
                        preferencia.usuarioId(), preferencia.widgetId(), preferencia.visivel(), preferencia.ordem(),
                        preferencia.configuracaoJson(), now, now));
        entity.atualizar(preferencia.visivel(), preferencia.ordem(), preferencia.configuracaoJson(), now);
        return toModel(repository.save(entity));
    }

    @Override
    public void excluir(PainelUsuarioPreferencia preferencia) {
        repository.deleteById(preferencia.id());
    }

    private PainelUsuarioPreferencia toModel(PainelUsuarioPreferenciaJpaEntity entity) {
        return new PainelUsuarioPreferencia(entity.getId(), entity.getEscolaId(), entity.getUsuarioId(), entity.getWidgetId(),
                entity.isVisivel(), entity.getOrdem(), entity.getConfiguracaoJson());
    }
}
