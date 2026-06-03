package br.com.escola.seguranca.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.seguranca.domain.model.PermissaoModel;

public interface PermissaoUseCasePort {

    List<PermissaoModel> listAll();

    Optional<PermissaoModel> findById(UUID id);

    PermissaoModel create(PermissaoModel domain);

    PermissaoModel update(UUID id, PermissaoModel domain);

    void delete(UUID id);

    boolean existsByCodigo(String codigo);
}