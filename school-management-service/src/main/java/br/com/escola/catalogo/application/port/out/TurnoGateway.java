package br.com.escola.catalogo.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.catalogo.application.dto.TurnoInput;
import br.com.escola.catalogo.application.dto.TurnoOutput;

public interface TurnoGateway {

    Optional<TurnoOutput> findById(UUID id);

    boolean existsById(UUID id);

    TurnoOutput save(TurnoInput input);

    TurnoOutput update(UUID id, TurnoInput input);

    List<TurnoOutput> findAll();
}
