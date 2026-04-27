package br.com.escola.studentmanagement.application.port.out;

import br.com.escola.studentmanagement.application.dto.AlunoInput;
import br.com.escola.studentmanagement.application.dto.AlunoOutput;

public interface AlunoCommandGateway {

    boolean existsByCpf(String cpf);

    AlunoOutput save(AlunoInput input);
}
