package br.com.escola.identityaccessservice.application.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.identityaccessservice.application.dto.UsuarioRequest;
import br.com.escola.identityaccessservice.application.model.UsuarioAdministrado;
import br.com.escola.identityaccessservice.application.port.in.AdministrarUsuarioUseCase;
import br.com.escola.identityaccessservice.application.port.out.SenhaProtegidaPort;
import br.com.escola.identityaccessservice.application.port.out.UsuarioAdministradoPort;

@Service
public class AdministracaoUsuarioService implements AdministrarUsuarioUseCase {

    private final UsuarioAdministradoPort usuarioAdministradoPort;
    private final SenhaProtegidaPort senhaProtegidaPort;

    public AdministracaoUsuarioService(
            UsuarioAdministradoPort usuarioAdministradoPort,
            SenhaProtegidaPort senhaProtegidaPort) {
        this.usuarioAdministradoPort = usuarioAdministradoPort;
        this.senhaProtegidaPort = senhaProtegidaPort;
    }

    @Override
    public List<UsuarioAdministrado> listar() {
        return usuarioAdministradoPort.listar();
    }

    @Override
    public UsuarioAdministrado buscar(UUID id) {
        return usuarioAdministradoPort.buscar(id);
    }

    @Override
    public UsuarioAdministrado criar(UsuarioRequest request) {
        return salvar(UUID.randomUUID(), request);
    }

    @Override
    public UsuarioAdministrado atualizar(UUID id, UsuarioRequest request) {
        usuarioAdministradoPort.buscar(id);
        return salvar(id, request);
    }

    @Override
    public void excluir(UUID id) {
        usuarioAdministradoPort.excluir(id);
    }

    private UsuarioAdministrado salvar(UUID id, UsuarioRequest request) {
        Set<UUID> perfilIds = request.perfilIds() == null ? Set.of() : Set.copyOf(request.perfilIds());
        if (perfilIds.isEmpty()) {
            throw new IllegalArgumentException("Usuario deve possuir ao menos um perfil");
        }
        return usuarioAdministradoPort.salvar(
                id,
                request.username().trim(),
                request.nome().trim(),
                request.email().trim().toLowerCase(),
                senhaProtegidaPort.proteger(request.senhaHash()),
                request.ativo() == null || request.ativo(),
                request.escolaId(),
                perfilIds);
    }
}
