package br.com.escola.identityaccessservice.application.port.out;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.escola.identityaccessservice.application.model.UsuarioAdministrado;

public interface UsuarioAdministradoPort {

    List<UsuarioAdministrado> listar();

    UsuarioAdministrado buscar(UUID id);

    UsuarioAdministrado salvar(
            UUID id,
            String username,
            String nome,
            String email,
            String senhaProtegida,
            boolean ativo,
            UUID escolaId,
            Set<UUID> perfilIds);

    void excluir(UUID id);
}
