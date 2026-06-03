package br.com.escola.catalogo.adapter.out.persistence;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.escola.catalogo.adapter.out.persistence.entity.TurnoEntity;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurnoJpaRepository;
import br.com.escola.catalogo.application.dto.TurnoInput;
import br.com.escola.catalogo.application.dto.TurnoOutput;
import br.com.escola.catalogo.application.port.out.TurnoGateway;
import br.com.escola.catalogo.domain.exception.TurnoNaoEncontradoException;

@Component
public class TurnoPersistenceGateway implements TurnoGateway {

    private final TurnoJpaRepository turnoJpaRepository;

    public TurnoPersistenceGateway(TurnoJpaRepository turnoJpaRepository) {
        this.turnoJpaRepository = turnoJpaRepository;
    }

    @Override
    public Optional<TurnoOutput> findById(UUID id) {
        return turnoJpaRepository.findById(id).map(this::toOutput);
    }

    @Override
    public boolean existsById(UUID id) {
        return turnoJpaRepository.existsById(id);
    }

    @Override
    public TurnoOutput save(TurnoInput input) {
        TurnoEntity entity = new TurnoEntity();
        entity.setCodigo(normalizarCodigo(input.codigo()));
        entity.setDescricao(input.descricao().trim());
        return toOutput(turnoJpaRepository.save(entity));
    }

    @Override
    public TurnoOutput update(UUID id, TurnoInput input) {
        TurnoEntity entity = turnoJpaRepository.findById(id)
                .orElseThrow(() -> new TurnoNaoEncontradoException(id));
        entity.setCodigo(normalizarCodigo(input.codigo()));
        entity.setDescricao(input.descricao().trim());
        return toOutput(turnoJpaRepository.save(entity));
    }

    @Override
    public List<TurnoOutput> findAll() {
        return turnoJpaRepository.findAll().stream().map(this::toOutput).toList();
    }

    private TurnoOutput toOutput(TurnoEntity entity) {
        return new TurnoOutput(entity.getId(), entity.getCodigo(), entity.getDescricao());
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }
}
