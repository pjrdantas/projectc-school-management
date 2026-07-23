package br.com.escola.identityaccessservice.application.port.out;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.escola.identityaccessservice.application.model.PerfilAdministrado;

public interface PerfilAdministradoPort {

    List<PerfilAdministrado> listar();

    PerfilAdministrado buscar(UUID id);

    PerfilAdministrado salvar(
            UUID id,
            String codigo,
            String nome,
            String descricao,
            Set<UUID> permissaoIds);

    void excluir(UUID id);
}
