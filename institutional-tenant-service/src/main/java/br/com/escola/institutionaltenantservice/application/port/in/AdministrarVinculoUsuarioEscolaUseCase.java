package br.com.escola.institutionaltenantservice.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.institutionaltenantservice.application.dto.VinculoUsuarioEscolaRequest;
import br.com.escola.institutionaltenantservice.application.model.ResultadoVinculoUsuarioEscola;
import br.com.escola.institutionaltenantservice.application.model.VinculoUsuarioEscola;

public interface AdministrarVinculoUsuarioEscolaUseCase {

    List<VinculoUsuarioEscola> listar(UUID usuarioId, UUID escolaId);

    VinculoUsuarioEscola buscar(UUID id);

    ResultadoVinculoUsuarioEscola vincular(VinculoUsuarioEscolaRequest request);

    void excluir(UUID id);
}
