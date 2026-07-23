package br.com.escola.peopleservice.application.port.out;

import java.util.UUID;

import br.com.escola.peopleservice.application.model.AlunoAlteracao;
import br.com.escola.peopleservice.application.model.AlunoAtualizado;

public interface AlunoAtualizacaoPort {

    AlunoAtualizado atualizar(UUID alunoId, AlunoAlteracao aluno);
}
