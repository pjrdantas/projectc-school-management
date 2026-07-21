package br.com.escola.peopleservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.peopleservice.application.model.AlunoConsulta;

public interface AlunoLeituraPort {

    List<AlunoConsulta> listar(String nome, UUID escolaId);

    Optional<AlunoConsulta> buscar(UUID alunoId, UUID escolaId);
}
