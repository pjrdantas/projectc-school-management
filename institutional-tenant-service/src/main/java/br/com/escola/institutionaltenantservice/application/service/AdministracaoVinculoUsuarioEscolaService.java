package br.com.escola.institutionaltenantservice.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.institutionaltenantservice.application.dto.VinculoUsuarioEscolaRequest;
import br.com.escola.institutionaltenantservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.institutionaltenantservice.application.model.ResultadoVinculoUsuarioEscola;
import br.com.escola.institutionaltenantservice.application.model.VinculoUsuarioEscola;
import br.com.escola.institutionaltenantservice.application.port.in.AdministrarVinculoUsuarioEscolaUseCase;
import br.com.escola.institutionaltenantservice.application.port.out.VinculoUsuarioEscolaPort;

@Service
public class AdministracaoVinculoUsuarioEscolaService implements AdministrarVinculoUsuarioEscolaUseCase {

    private final VinculoUsuarioEscolaPort vinculoUsuarioEscolaPort;

    public AdministracaoVinculoUsuarioEscolaService(VinculoUsuarioEscolaPort vinculoUsuarioEscolaPort) {
        this.vinculoUsuarioEscolaPort = vinculoUsuarioEscolaPort;
    }

    @Override
    public List<VinculoUsuarioEscola> listar(UUID usuarioId, UUID escolaId) {
        return vinculoUsuarioEscolaPort.listar(usuarioId, escolaId);
    }

    @Override
    public VinculoUsuarioEscola buscar(UUID id) {
        return vinculoUsuarioEscolaPort.buscar(id);
    }

    @Override
    public ResultadoVinculoUsuarioEscola vincular(VinculoUsuarioEscolaRequest request) {
        if (!vinculoUsuarioEscolaPort.escolaExiste(request.escolaId())) {
            throw new RecursoNaoEncontradoException("Escola nao encontrada");
        }
        return vinculoUsuarioEscolaPort.garantir(new VinculoUsuarioEscola(
                UUID.randomUUID(), request.usuarioId(), request.escolaId(), LocalDateTime.now()));
    }

    @Override
    public void excluir(UUID id) {
        vinculoUsuarioEscolaPort.excluir(id);
    }
}
