package br.com.escola.accesscontrol.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.accesscontrol.domain.model.PerfilModel;

public interface PerfilUseCasePort {

    PerfilModel create(PerfilModel domain);

    PerfilModel update(UUID id, PerfilModel domain);

    Optional<PerfilModel> findById(UUID id);

    List<PerfilModel> listAll();

    void delete(UUID id);

    boolean existsByCodigo(String codigo);
}