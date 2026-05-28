package br.com.escola.academiccatalog.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.escola.academiccatalog.adapter.out.persistence.entity.SerieEntity;
import br.com.escola.academiccatalog.adapter.out.persistence.repository.NivelEnsinoJpaRepository;
import br.com.escola.academiccatalog.adapter.out.persistence.repository.SerieJpaRepository;
import br.com.escola.academiccatalog.application.dto.SerieInput;
import br.com.escola.academiccatalog.application.dto.SerieOutput;
import br.com.escola.academiccatalog.application.port.out.SerieGateway;
import br.com.escola.academiccatalog.domain.exception.SerieNaoEncontradaException;

@Component
public class SeriePersistenceGateway implements SerieGateway {

    private final SerieJpaRepository serieJpaRepository;
    private final NivelEnsinoJpaRepository nivelEnsinoJpaRepository;

    public SeriePersistenceGateway(
            SerieJpaRepository serieJpaRepository,
            NivelEnsinoJpaRepository nivelEnsinoJpaRepository) {
        this.serieJpaRepository = serieJpaRepository;
        this.nivelEnsinoJpaRepository = nivelEnsinoJpaRepository;
    }

    @Override
    public Optional<SerieOutput> findById(UUID id) {
        return serieJpaRepository.findById(id).map(this::toOutput);
    }

    @Override
    public boolean existsById(UUID id) {
        return serieJpaRepository.existsById(id);
    }

    @Override
    public SerieOutput save(SerieInput input) {
        SerieEntity entity = new SerieEntity();
        entity.setNome(input.nome());
        entity.setOrdem(input.ordem());
        entity.setNivelEnsino(resolveNivelEnsino(input.nivelEnsino()));
        entity.setCreatedAt(LocalDateTime.now());
        return toOutput(serieJpaRepository.save(entity));
    }

    @Override
    public SerieOutput update(UUID id, SerieInput input) {
        SerieEntity entity = serieJpaRepository.findById(id)
                .orElseThrow(() -> new SerieNaoEncontradaException(id));
        entity.setNome(input.nome());
        entity.setOrdem(input.ordem());
        entity.setNivelEnsino(resolveNivelEnsino(input.nivelEnsino()));
        return toOutput(serieJpaRepository.save(entity));
    }

    @Override
    public List<SerieOutput> findAll() {
        return serieJpaRepository.findAll().stream().map(this::toOutput).toList();
    }

    private SerieOutput toOutput(SerieEntity entity) {
        return new SerieOutput(
                entity.getId(),
                entity.getNome(),
                entity.getOrdem(),
                entity.getNivelEnsino(),
                entity.getCreatedAt());
    }

    private br.com.escola.academiccatalog.adapter.out.persistence.entity.NivelEnsinoEntity resolveNivelEnsino(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        String codigoNormalizado = codigo.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        return nivelEnsinoJpaRepository.findByCodigoIgnoreCase(codigoNormalizado)
                .orElseThrow(() -> new IllegalArgumentException("Nível de ensino não encontrado: " + codigo));
    }
}
