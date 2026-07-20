package br.com.escola.institutionaltenantservice.application.port.out;

import java.util.List;
import java.util.UUID;

import br.com.escola.institutionaltenantservice.application.model.EscolaAdministrada;

public interface EscolaAdministradaPort {

    List<EscolaAdministrada> listar();

    EscolaAdministrada buscar(UUID id);

    EscolaAdministrada salvar(EscolaAdministrada escola);

    void excluir(UUID id);
}
