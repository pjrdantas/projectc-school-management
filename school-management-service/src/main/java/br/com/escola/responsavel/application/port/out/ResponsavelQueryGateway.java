package br.com.escola.responsavel.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.responsavel.application.dto.ResponsavelOutput;

public interface ResponsavelQueryGateway {

    Optional<ResponsavelOutput> findById(UUID id);

    List<ResponsavelOutput> findAll();

    boolean existsById(UUID id);

    boolean hasAlunosVinculados(UUID id);
}
