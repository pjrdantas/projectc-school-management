package br.com.escola.identityaccessservice.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.identityaccessservice.application.dto.PerfilRequest;
import br.com.escola.identityaccessservice.application.model.PerfilAdministrado;

public interface AdministrarPerfilUseCase {

    List<PerfilAdministrado> listar();

    PerfilAdministrado buscar(UUID id);

    PerfilAdministrado criar(PerfilRequest request);

    PerfilAdministrado atualizar(UUID id, PerfilRequest request);

    void excluir(UUID id);
}
