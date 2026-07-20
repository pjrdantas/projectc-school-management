package br.com.escola.identityaccessservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.identityaccessservice.application.dto.PermissaoRequest;
import br.com.escola.identityaccessservice.application.model.PermissaoAdministrada;
import br.com.escola.identityaccessservice.application.port.in.AdministrarPermissaoUseCase;
import br.com.escola.identityaccessservice.application.port.out.PermissaoAdministradaPort;

@Service
public class AdministracaoPermissaoService implements AdministrarPermissaoUseCase {

    private final PermissaoAdministradaPort permissaoAdministradaPort;

    public AdministracaoPermissaoService(PermissaoAdministradaPort permissaoAdministradaPort) {
        this.permissaoAdministradaPort = permissaoAdministradaPort;
    }

    @Override
    public List<PermissaoAdministrada> listar() {
        return permissaoAdministradaPort.listar();
    }

    @Override
    public PermissaoAdministrada buscar(UUID id) {
        return permissaoAdministradaPort.buscar(id);
    }

    @Override
    public PermissaoAdministrada criar(PermissaoRequest request) {
        return permissaoAdministradaPort.salvar(
                UUID.randomUUID(), normalizarCodigo(request.codigo()), normalizarDescricao(request.descricao()));
    }

    @Override
    public PermissaoAdministrada atualizar(UUID id, PermissaoRequest request) {
        permissaoAdministradaPort.buscar(id);
        return permissaoAdministradaPort.salvar(
                id, normalizarCodigo(request.codigo()), normalizarDescricao(request.descricao()));
    }

    @Override
    public void excluir(UUID id) {
        permissaoAdministradaPort.excluir(id);
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase();
    }

    private String normalizarDescricao(String descricao) {
        return descricao == null || descricao.isBlank() ? null : descricao.trim();
    }
}
