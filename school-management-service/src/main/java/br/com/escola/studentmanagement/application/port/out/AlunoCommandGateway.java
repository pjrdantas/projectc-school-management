package br.com.escola.studentmanagement.application.port.out;

import org.springframework.lang.NonNull;

import br.com.escola.studentmanagement.application.dto.AlunoInput;
import br.com.escola.studentmanagement.application.dto.AlunoOutput;

public interface AlunoCommandGateway {

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, @NonNull Long id);

    AlunoOutput save(AlunoInput input);

    AlunoOutput update(@NonNull Long id, AlunoInput input);

    void deleteById(@NonNull Long id);
}
