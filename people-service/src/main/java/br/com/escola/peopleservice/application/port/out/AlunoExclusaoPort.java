package br.com.escola.peopleservice.application.port.out;

import java.util.UUID;

public interface AlunoExclusaoPort {

    void excluir(UUID alunoId, UUID escolaId);
}
