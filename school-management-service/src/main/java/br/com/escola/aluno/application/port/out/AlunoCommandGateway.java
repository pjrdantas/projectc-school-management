package br.com.escola.aluno.application.port.out;

import java.util.UUID;

import org.springframework.lang.NonNull;

import br.com.escola.aluno.application.dto.AlunoInput;
import br.com.escola.aluno.application.dto.AlunoOutput;

public interface AlunoCommandGateway {

    boolean existsByCpf(String cpf, UUID escolaId);

    boolean existsByCpfAndIdNot(String cpf, UUID escolaId, @NonNull UUID id);

    AlunoOutput save(AlunoInput input);

    AlunoOutput update(@NonNull UUID id, AlunoInput input);

    void deleteById(@NonNull UUID id);
}
