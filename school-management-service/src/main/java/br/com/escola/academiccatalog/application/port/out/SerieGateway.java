package br.com.escola.academiccatalog.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.academiccatalog.application.dto.SerieInput;
import br.com.escola.academiccatalog.application.dto.SerieOutput;

public interface SerieGateway {

    Optional<SerieOutput> findById(UUID id);

    boolean existsById(UUID id);

    SerieOutput save(SerieInput input);

    SerieOutput update(UUID id, SerieInput input);

    List<SerieOutput> findAll();
}
