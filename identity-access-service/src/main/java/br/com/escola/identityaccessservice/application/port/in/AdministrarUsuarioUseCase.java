package br.com.escola.identityaccessservice.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.identityaccessservice.application.dto.UsuarioRequest;
import br.com.escola.identityaccessservice.application.model.UsuarioAdministrado;

public interface AdministrarUsuarioUseCase {

    List<UsuarioAdministrado> listar();

    UsuarioAdministrado buscar(UUID id);

    UsuarioAdministrado criar(UsuarioRequest request);

    UsuarioAdministrado atualizar(UUID id, UsuarioRequest request);

    void excluir(UUID id);
}
