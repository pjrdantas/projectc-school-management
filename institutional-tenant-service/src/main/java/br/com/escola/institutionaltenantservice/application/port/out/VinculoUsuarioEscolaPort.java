package br.com.escola.institutionaltenantservice.application.port.out;

import java.util.List;
import java.util.UUID;

import br.com.escola.institutionaltenantservice.application.model.ResultadoVinculoUsuarioEscola;
import br.com.escola.institutionaltenantservice.application.model.VinculoUsuarioEscola;

public interface VinculoUsuarioEscolaPort {

    List<VinculoUsuarioEscola> listar(UUID usuarioId, UUID escolaId);

    VinculoUsuarioEscola buscar(UUID id);

    boolean escolaExiste(UUID escolaId);

    ResultadoVinculoUsuarioEscola garantir(VinculoUsuarioEscola vinculo);

    void excluir(UUID id);
}
