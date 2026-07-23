package br.com.escola.identityaccessservice.application.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.identityaccessservice.application.dto.PerfilRequest;
import br.com.escola.identityaccessservice.application.model.PerfilAdministrado;
import br.com.escola.identityaccessservice.application.port.in.AdministrarPerfilUseCase;
import br.com.escola.identityaccessservice.application.port.out.PerfilAdministradoPort;

@Service
public class AdministracaoPerfilService implements AdministrarPerfilUseCase {

    private final PerfilAdministradoPort perfilAdministradoPort;

    public AdministracaoPerfilService(PerfilAdministradoPort perfilAdministradoPort) {
        this.perfilAdministradoPort = perfilAdministradoPort;
    }

    @Override
    public List<PerfilAdministrado> listar() {
        return perfilAdministradoPort.listar();
    }

    @Override
    public PerfilAdministrado buscar(UUID id) {
        return perfilAdministradoPort.buscar(id);
    }

    @Override
    public PerfilAdministrado criar(PerfilRequest request) {
        return salvar(UUID.randomUUID(), request);
    }

    @Override
    public PerfilAdministrado atualizar(UUID id, PerfilRequest request) {
        perfilAdministradoPort.buscar(id);
        return salvar(id, request);
    }

    @Override
    public void excluir(UUID id) {
        perfilAdministradoPort.excluir(id);
    }

    private PerfilAdministrado salvar(UUID id, PerfilRequest request) {
        return perfilAdministradoPort.salvar(
                id,
                request.codigo().trim().toUpperCase(),
                request.nome().trim(),
                request.descricao() == null || request.descricao().isBlank()
                        ? null
                        : request.descricao().trim(),
                request.permissaoIds() == null ? Set.of() : Set.copyOf(request.permissaoIds()));
    }
}
