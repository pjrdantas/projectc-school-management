package br.com.escola.identityaccessservice.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.identityaccessservice.application.dto.PermissaoRequest;
import br.com.escola.identityaccessservice.application.model.PermissaoAdministrada;

public interface AdministrarPermissaoUseCase {

    List<PermissaoAdministrada> listar();

    PermissaoAdministrada buscar(UUID id);

    PermissaoAdministrada criar(PermissaoRequest request);

    PermissaoAdministrada atualizar(UUID id, PermissaoRequest request);

    void excluir(UUID id);
}
