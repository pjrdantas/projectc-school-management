package br.com.escola.catalogo.adapter.out.persistence;

import java.util.UUID;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.catalogo.adapter.out.persistence.entity.PeriodoLetivoEntity;
import br.com.escola.catalogo.adapter.out.persistence.entity.SerieEntity;
import br.com.escola.catalogo.adapter.out.persistence.entity.TurmaEntity;
import br.com.escola.catalogo.adapter.out.persistence.repository.PeriodoLetivoJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.repository.SerieJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurmaJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurnoJpaRepository;
import br.com.escola.catalogo.application.dto.TurmaInput;
import br.com.escola.catalogo.application.dto.TurmaOutput;
import br.com.escola.catalogo.application.port.out.TurmaGateway;
import br.com.escola.catalogo.domain.exception.SerieNaoEncontradaException;
import br.com.escola.catalogo.domain.exception.TurmaNaoEncontradaException;
import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;
import br.com.escola.institucional.adapter.out.persistence.repository.EscolaJpaRepository;
import br.com.escola.institucional.application.service.EscolaTenantService;

@Component
public class TurmaPersistenceGateway implements TurmaGateway {

    private final TurmaJpaRepository turmaJpaRepository;
    private final PeriodoLetivoJpaRepository periodoLetivoJpaRepository;
    private final SerieJpaRepository serieJpaRepository;
    private final TurnoJpaRepository turnoJpaRepository;
    private final EscolaJpaRepository escolaJpaRepository;
    private final EscolaTenantService escolaTenantService;

    public TurmaPersistenceGateway(
            TurmaJpaRepository turmaJpaRepository,
            PeriodoLetivoJpaRepository periodoLetivoJpaRepository,
            SerieJpaRepository serieJpaRepository,
            TurnoJpaRepository turnoJpaRepository,
            EscolaJpaRepository escolaJpaRepository,
            EscolaTenantService escolaTenantService) {
        this.turmaJpaRepository = turmaJpaRepository;
        this.periodoLetivoJpaRepository = periodoLetivoJpaRepository;
        this.serieJpaRepository = serieJpaRepository;
        this.turnoJpaRepository = turnoJpaRepository;
        this.escolaJpaRepository = escolaJpaRepository;
        this.escolaTenantService = escolaTenantService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TurmaOutput> findById(@NonNull UUID id) {
        UUID escolaId = escolaTenantService.obterOuCriarEscolaPadrao().getId();
        return turmaJpaRepository.findByIdAndEscola_Id(id, escolaId).map(this::toOutput);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TurmaOutput> findByCodigoAndPeriodoLetivoId(String codigo, UUID periodoLetivoId) {
        UUID escolaId = escolaTenantService.obterOuCriarEscolaPadrao().getId();
        return turmaJpaRepository.findByCodigoAndPeriodoLetivo_IdAndEscola_Id(codigo, periodoLetivoId, escolaId)
                .map(this::toOutput);
    }

    @Override
    @Transactional
    public TurmaOutput save(TurmaInput input) {
        EscolaEntity escola = resolverEscola(input.escolaId());
        PeriodoLetivoEntity periodo = periodoLetivoJpaRepository.findByIdAndEscola_Id(input.periodoLetivoId(), escola.getId())
                .orElseThrow(() -> new br.com.escola.catalogo.domain.exception.PeriodoLetivoNaoEncontradoException(input.periodoLetivoId()));
        SerieEntity serie = serieJpaRepository.findByIdAndEscola_Id(input.serieId(), escola.getId())
                .orElseThrow(() -> new SerieNaoEncontradaException(input.serieId()));
        TurmaEntity entity = new TurmaEntity();
        entity.setCodigo(input.codigo());
        entity.setNome(input.nome());
        entity.setCapacidade(input.capacidade());
        entity.setPeriodoLetivo(periodo);
        entity.setSerie(serie);
        entity.setTurno(resolveTurno(input.turno()));
        entity.setEscola(escola);
        entity.setAtivo(resolveAtivo(input.status()));
        entity.setCreatedAt(LocalDateTime.now());
        return toOutput(turmaJpaRepository.save(entity));
    }

    @Override
    @Transactional
    public TurmaOutput update(UUID id, TurmaInput input) {
        EscolaEntity escola = resolverEscola(input.escolaId());
        TurmaEntity entity = turmaJpaRepository.findByIdAndEscola_Id(id, escola.getId())
                .orElseThrow(() -> new TurmaNaoEncontradaException(id));
        PeriodoLetivoEntity periodo = periodoLetivoJpaRepository.findByIdAndEscola_Id(input.periodoLetivoId(), escola.getId())
                .orElseThrow(() -> new br.com.escola.catalogo.domain.exception.PeriodoLetivoNaoEncontradoException(input.periodoLetivoId()));
        SerieEntity serie = serieJpaRepository.findByIdAndEscola_Id(input.serieId(), escola.getId())
                .orElseThrow(() -> new SerieNaoEncontradaException(input.serieId()));

        entity.setCodigo(input.codigo());
        entity.setNome(input.nome());
        entity.setCapacidade(input.capacidade());
        entity.setPeriodoLetivo(periodo);
        entity.setSerie(serie);
        entity.setTurno(resolveTurno(input.turno()));
        entity.setEscola(escola);
        entity.setAtivo(resolveAtivo(input.status()));

        return toOutput(turmaJpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurmaOutput> findAll() {
        UUID escolaId = escolaTenantService.obterOuCriarEscolaPadrao().getId();
        return turmaJpaRepository.findAllByEscola_Id(escolaId).stream().map(this::toOutput).toList();
    }

    private TurmaOutput toOutput(TurmaEntity entity) {
        return new TurmaOutput(
                entity.getId(),
                entity.getCodigo(),
                entity.getNome(),
                entity.getCapacidade(),
                entity.getPeriodoLetivo().getId(),
                entity.getSerie().getId(),
                entity.getSerie().getNome(),
                entity.getTurno(),
                entity.getStatus(),
                entity.getEscola().getId(),
                entity.getEscola().getNome(),
                entity.getCreatedAt());
    }

    private EscolaEntity resolverEscola(UUID escolaId) {
        if (escolaId == null) {
            return escolaTenantService.obterOuCriarEscolaPadrao();
        }
        return escolaJpaRepository.findById(escolaId)
                .orElseThrow(() -> new IllegalArgumentException("Escola não encontrada."));
    }

    private br.com.escola.catalogo.adapter.out.persistence.entity.TurnoEntity resolveTurno(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        return turnoJpaRepository.findByCodigoIgnoreCase(codigo.trim())
                .orElseThrow(() -> new IllegalArgumentException("Turno não encontrado: " + codigo));
    }

    private Boolean resolveAtivo(String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        String normalized = status.trim();
        return !normalized.equalsIgnoreCase("INATIVA")
                && !normalized.equalsIgnoreCase("INATIVO")
                && !normalized.equalsIgnoreCase("false");
    }
}
