package br.com.escola.peopleservice.application.port.out;

import br.com.escola.peopleservice.application.model.AlunoCriado;
import br.com.escola.peopleservice.application.model.AlunoNovo;

public interface AlunoCriacaoPort {

    AlunoCriado criar(AlunoNovo aluno);
}
