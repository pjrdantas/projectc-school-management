package br.com.escola.catalogo.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.catalogo.application.dto.SerieInput;
import br.com.escola.catalogo.application.dto.SerieOutput;

public interface SerieGateway {

    Optional<SerieOutput> findById(UUID id);

    boolean existsById(UUID id);

    SerieOutput save(SerieInput input);

    SerieOutput update(UUID id, SerieInput input);

    List<SerieOutput> findAll();
}
