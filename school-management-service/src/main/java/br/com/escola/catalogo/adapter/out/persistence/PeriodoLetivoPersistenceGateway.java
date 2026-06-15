package br.com.escola.catalogo.adapter.out.persistence;

import java.util.UUID;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.catalogo.adapter.out.persistence.entity.PeriodoLetivoEntity;
import br.com.escola.catalogo.adapter.out.persistence.repository.PeriodoLetivoJpaRepository;
import br.com.escola.catalogo.application.dto.PeriodoLetivoInput;
import br.com.escola.catalogo.application.dto.PeriodoLetivoOutput;
import br.com.escola.catalogo.application.port.out.PeriodoLetivoGateway;
import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;
import br.com.escola.institucional.adapter.out.persistence.repository.EscolaJpaRepository;
import br.com.escola.institucional.application.port.EscolaContextoPort;

@Component
@Transactional
public class PeriodoLetivoPersistenceGateway implements PeriodoLetivoGateway {

    private final PeriodoLetivoJpaRepository periodoLetivoJpaRepository;
    private final EscolaJpaRepository escolaJpaRepository;
    private final EscolaContextoPort escolaContextoPort;

    public PeriodoLetivoPersistenceGateway(
            PeriodoLetivoJpaRepository periodoLetivoJpaRepository,
            EscolaJpaRepository escolaJpaRepository,
            EscolaContextoPort escolaContextoPort) {
        this.periodoLetivoJpaRepository = periodoLetivoJpaRepository;
        this.escolaJpaRepository = escolaJpaRepository;
        this.escolaContextoPort = escolaContextoPort;
    }

    @Override
    public Optional<PeriodoLetivoOutput> findById(@NonNull UUID id) {
        UUID escolaId = resolverEscolaPadraoId();
        return periodoLetivoJpaRepository.findByIdAndEscola_Id(id, escolaId).map(this::toOutput);
    }

    @Override
    public boolean existsById(@NonNull UUID id) {
        UUID escolaId = resolverEscolaPadraoId();
        return periodoLetivoJpaRepository.existsByIdAndEscola_Id(id, escolaId);
    }

    @Override
    public PeriodoLetivoOutput save(PeriodoLetivoInput input) {
        EscolaEntity escola = resolverEscola(input.escolaId());
        PeriodoLetivoEntity entity = new PeriodoLetivoEntity();
        entity.setNome(input.nome());
        entity.setAno(resolveAno(input));
        entity.setDataInicio(input.dataInicio());
        entity.setDataFim(input.dataFim());
        entity.setEscola(escola);
        entity.setAtivo(true);
        entity.setCreatedAt(LocalDateTime.now());
        return toOutput(periodoLetivoJpaRepository.save(entity));
    }

    @Override
    public List<PeriodoLetivoOutput> findAll() {
        UUID escolaId = resolverEscolaPadraoId();
        return periodoLetivoJpaRepository.findAllByEscola_Id(escolaId).stream().map(this::toOutput).toList();
    }

    private PeriodoLetivoOutput toOutput(PeriodoLetivoEntity entity) {
        return new PeriodoLetivoOutput(
                entity.getId(),
                entity.getNome(),
                entity.getAno(),
                entity.getDataInicio(),
                entity.getDataFim(),
                entity.getAtivo(),
                entity.getEscola().getId(),
                entity.getEscola().getNome(),
                entity.getCreatedAt());
    }

    private EscolaEntity resolverEscola(UUID escolaId) {
        if (escolaId == null) {
            escolaId = resolverEscolaPadraoId();
        }
        return escolaJpaRepository.findById(escolaId)
                .orElseThrow(() -> new IllegalArgumentException("Escola não encontrada."));
    }

    private UUID resolverEscolaPadraoId() {
        return escolaContextoPort.obterContextoPadrao().escolaId();
    }

    private Integer resolveAno(PeriodoLetivoInput input) {
        if (input.ano() != null) {
            return input.ano();
        }
        return input.dataInicio().getYear();
    }
}
