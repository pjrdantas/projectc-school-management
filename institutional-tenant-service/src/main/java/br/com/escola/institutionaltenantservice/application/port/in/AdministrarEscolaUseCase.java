package br.com.escola.institutionaltenantservice.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.institutionaltenantservice.application.dto.EscolaRequest;
import br.com.escola.institutionaltenantservice.application.model.EscolaAdministrada;

public interface AdministrarEscolaUseCase {

    List<EscolaAdministrada> listar();

    EscolaAdministrada buscar(UUID id);

    EscolaAdministrada criar(EscolaRequest request);

    EscolaAdministrada atualizar(UUID id, EscolaRequest request);

    void excluir(UUID id);
}
