package br.com.escola.studentmanagement.application.port.out;

import java.util.UUID;

import org.springframework.lang.NonNull;

import br.com.escola.studentmanagement.application.dto.AlunoInput;
import br.com.escola.studentmanagement.application.dto.AlunoOutput;

public interface AlunoCommandGateway {

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, @NonNull UUID id);

    AlunoOutput save(AlunoInput input);

    AlunoOutput update(@NonNull UUID id, AlunoInput input);

    void deleteById(@NonNull UUID id);
}
