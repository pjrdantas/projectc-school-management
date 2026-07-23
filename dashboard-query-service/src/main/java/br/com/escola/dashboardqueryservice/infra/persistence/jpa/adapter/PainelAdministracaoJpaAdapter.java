package br.com.escola.dashboardqueryservice.infra.persistence.jpa.adapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.escola.dashboardqueryservice.application.port.out.PainelAdministracaoStorePort;
import br.com.escola.dashboardqueryservice.domain.model.PainelConfiguracao;
import br.com.escola.dashboardqueryservice.domain.model.PainelPublico;
import br.com.escola.dashboardqueryservice.infra.persistence.jpa.entity.PainelConfiguracaoJpaEntity;
import br.com.escola.dashboardqueryservice.infra.persistence.jpa.entity.PainelPublicoJpaEntity;
import br.com.escola.dashboardqueryservice.infra.persistence.jpa.repository.PainelConfiguracaoJpaRepository;
import br.com.escola.dashboardqueryservice.infra.persistence.jpa.repository.PainelPublicoJpaRepository;
import br.com.escola.dashboardqueryservice.infra.persistence.jpa.repository.PainelWidgetJpaRepository;

@Component
public class PainelAdministracaoJpaAdapter implements PainelAdministracaoStorePort {

    private final PainelPublicoJpaRepository publicoRepository;
    private final PainelConfiguracaoJpaRepository painelRepository;
    private final PainelWidgetJpaRepository widgetRepository;

    public PainelAdministracaoJpaAdapter(
            PainelPublicoJpaRepository publicoRepository,
            PainelConfiguracaoJpaRepository painelRepository,
            PainelWidgetJpaRepository widgetRepository) {
        this.publicoRepository = publicoRepository;
        this.painelRepository = painelRepository;
        this.widgetRepository = widgetRepository;
    }

    @Override
    public Optional<PainelPublico> buscarPublico(UUID escolaId, UUID publicoId) {
        return publicoRepository.findByIdAndEscolaId(publicoId, escolaId).map(this::toPublico);
    }

    @Override
    public Optional<PainelPublico> buscarPublicoPorCodigo(UUID escolaId, String codigo) {
        return publicoRepository.findByEscolaIdAndCodigoIgnoreCase(escolaId, codigo).map(this::toPublico);
    }

    @Override
    public List<PainelPublico> listarPublicos(UUID escolaId) {
        return publicoRepository.findByEscolaIdOrderByCodigoAsc(escolaId).stream().map(this::toPublico).toList();
    }

    @Override
    public PainelPublico salvarPublico(PainelPublico publico) {
        LocalDateTime now = LocalDateTime.now();
        PainelPublicoJpaEntity entity = publicoRepository.findByIdAndEscolaId(publico.id(), publico.escolaId())
                .orElseGet(() -> new PainelPublicoJpaEntity(
                        publico.id(), publico.escolaId(), publico.codigo(), publico.descricao(), now, now));
        entity.atualizar(publico.codigo(), publico.descricao(), now);
        return toPublico(publicoRepository.save(entity));
    }

    @Override
    public boolean existePainelParaPublico(UUID escolaId, UUID publicoId) {
        return painelRepository.existsByEscolaIdAndPublicoId(escolaId, publicoId);
    }

    @Override
    public void excluirPublico(PainelPublico publico) {
        publicoRepository.deleteById(publico.id());
    }

    @Override
    public Optional<PainelConfiguracao> buscarPainel(UUID escolaId, UUID painelId) {
        return painelRepository.findByIdAndEscolaId(painelId, escolaId).map(this::toPainel);
    }

    @Override
    public Optional<PainelConfiguracao> buscarPainelPorCodigo(UUID escolaId, String codigo) {
        return painelRepository.findByEscolaIdAndCodigoIgnoreCase(escolaId, codigo).map(this::toPainel);
    }

    @Override
    public List<PainelConfiguracao> listarPaineis(UUID escolaId, UUID publicoId, String publicoCodigo) {
        if (publicoId != null) {
            return painelRepository.findByEscolaIdAndPublicoIdOrderByCodigoAsc(escolaId, publicoId).stream().map(this::toPainel).toList();
        }
        return painelRepository.findByEscolaIdOrderByCodigoAsc(escolaId).stream().map(this::toPainel).toList();
    }

    @Override
    public PainelConfiguracao salvarPainel(PainelConfiguracao painel) {
        LocalDateTime now = LocalDateTime.now();
        PainelConfiguracaoJpaEntity entity = painelRepository.findByIdAndEscolaId(painel.id(), painel.escolaId())
                .orElseGet(() -> new PainelConfiguracaoJpaEntity(
                        painel.id(), painel.escolaId(), painel.publicoId(), painel.codigo(), painel.nome(),
                        painel.descricao(), painel.ativo(), now, now));
        entity.atualizar(painel.publicoId(), painel.codigo(), painel.nome(), painel.descricao(), painel.ativo(), now);
        return toPainel(painelRepository.save(entity));
    }

    @Override
    public boolean existeWidgetParaPainel(UUID escolaId, UUID painelId) {
        return widgetRepository.existsByEscolaIdAndPainelId(escolaId, painelId);
    }

    @Override
    public void excluirPainel(PainelConfiguracao painel) {
        painelRepository.deleteById(painel.id());
    }

    private PainelPublico toPublico(PainelPublicoJpaEntity entity) {
        return new PainelPublico(entity.getId(), entity.getEscolaId(), entity.getCodigo(), entity.getDescricao());
    }

    private PainelConfiguracao toPainel(PainelConfiguracaoJpaEntity entity) {
        return new PainelConfiguracao(entity.getId(), entity.getEscolaId(), entity.getPublicoId(), entity.getCodigo(),
                entity.getNome(), entity.getDescricao(), entity.isAtivo());
    }
}
