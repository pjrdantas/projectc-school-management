package br.com.escola.identityaccessservice.application.port.out;

import java.util.List;
import java.util.UUID;

import br.com.escola.identityaccessservice.application.model.PermissaoAdministrada;

public interface PermissaoAdministradaPort {

    List<PermissaoAdministrada> listar();

    PermissaoAdministrada buscar(UUID id);

    PermissaoAdministrada salvar(UUID id, String codigo, String descricao);

    void excluir(UUID id);
}
