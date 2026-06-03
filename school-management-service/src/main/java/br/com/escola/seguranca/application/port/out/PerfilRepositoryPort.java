package br.com.escola.seguranca.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.lang.NonNull;

import br.com.escola.seguranca.domain.model.PerfilModel;

public interface PerfilRepositoryPort {

    PerfilModel create(@NonNull PerfilModel domain);

    Optional<PerfilModel> findById(UUID id);

    Optional<PerfilModel> findByCodigo(String codigo);

    PerfilModel update(@NonNull UUID id, @NonNull PerfilModel domain);

    List<PerfilModel> listAll();

    void delete(@NonNull UUID id);

    boolean existsByCodigo(String codigo);
}