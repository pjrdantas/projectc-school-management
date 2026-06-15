package br.com.escola.catalogo.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.catalogo.adapter.out.persistence.entity.SerieEntity;
import br.com.escola.catalogo.adapter.out.persistence.repository.NivelEnsinoJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.repository.SerieJpaRepository;
import br.com.escola.catalogo.application.dto.SerieInput;
import br.com.escola.catalogo.application.dto.SerieOutput;
import br.com.escola.catalogo.application.port.out.SerieGateway;
import br.com.escola.catalogo.domain.exception.SerieNaoEncontradaException;
import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;
import br.com.escola.institucional.adapter.out.persistence.repository.EscolaJpaRepository;
import br.com.escola.institucional.application.port.EscolaContextoPort;

@Component
@Transactional
public class SeriePersistenceGateway implements SerieGateway {

    private final SerieJpaRepository serieJpaRepository;
    private final NivelEnsinoJpaRepository nivelEnsinoJpaRepository;
    private final EscolaJpaRepository escolaJpaRepository;
    private final EscolaContextoPort escolaContextoPort;

    public SeriePersistenceGateway(
            SerieJpaRepository serieJpaRepository,
            NivelEnsinoJpaRepository nivelEnsinoJpaRepository,
            EscolaJpaRepository escolaJpaRepository,
            EscolaContextoPort escolaContextoPort) {
        this.serieJpaRepository = serieJpaRepository;
        this.nivelEnsinoJpaRepository = nivelEnsinoJpaRepository;
        this.escolaJpaRepository = escolaJpaRepository;
        this.escolaContextoPort = escolaContextoPort;
    }

    @Override
    public Optional<SerieOutput> findById(UUID id) {
        UUID escolaId = resolverEscolaPadraoId();
        return serieJpaRepository.findByIdAndEscola_Id(id, escolaId).map(this::toOutput);
    }

    @Override
    public boolean existsById(UUID id) {
        UUID escolaId = resolverEscolaPadraoId();
        return serieJpaRepository.existsByIdAndEscola_Id(id, escolaId);
    }

    @Override
    public SerieOutput save(SerieInput input) {
        EscolaEntity escola = resolverEscola(input.escolaId());
        SerieEntity entity = new SerieEntity();
        entity.setNome(input.nome());
        entity.setOrdem(input.ordem());
        entity.setNivelEnsino(resolveNivelEnsino(input.nivelEnsino()));
        entity.setEscola(escola);
        entity.setCreatedAt(LocalDateTime.now());
        return toOutput(serieJpaRepository.save(entity));
    }

    @Override
    public SerieOutput update(UUID id, SerieInput input) {
        EscolaEntity escola = resolverEscola(input.escolaId());
        SerieEntity entity = serieJpaRepository.findByIdAndEscola_Id(id, escola.getId())
                .orElseThrow(() -> new SerieNaoEncontradaException(id));
        entity.setNome(input.nome());
        entity.setOrdem(input.ordem());
        entity.setNivelEnsino(resolveNivelEnsino(input.nivelEnsino()));
        entity.setEscola(escola);
        return toOutput(serieJpaRepository.save(entity));
    }

    @Override
    public List<SerieOutput> findAll() {
        UUID escolaId = resolverEscolaPadraoId();
        return serieJpaRepository.findAllByEscola_Id(escolaId).stream().map(this::toOutput).toList();
    }

    private SerieOutput toOutput(SerieEntity entity) {
        return new SerieOutput(
                entity.getId(),
                entity.getNome(),
                entity.getOrdem(),
                entity.getNivelEnsino(),
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

    private br.com.escola.catalogo.adapter.out.persistence.entity.NivelEnsinoEntity resolveNivelEnsino(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        String codigoNormalizado = codigo.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        return nivelEnsinoJpaRepository.findByCodigoIgnoreCase(codigoNormalizado)
                .orElseThrow(() -> new IllegalArgumentException("Nível de ensino não encontrado: " + codigo));
    }
}
