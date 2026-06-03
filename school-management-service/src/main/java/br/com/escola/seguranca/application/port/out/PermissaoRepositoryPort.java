package br.com.escola.seguranca.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.seguranca.domain.model.PermissaoModel;

public interface PermissaoRepositoryPort {

    PermissaoModel save(PermissaoModel model);

    Optional<PermissaoModel> findById(UUID id);

    List<PermissaoModel> findAll();

    void deleteById(UUID id);

    boolean existsByCodigo(String codigo);

    Optional<PermissaoModel> findByCodigo(String codigo);
}