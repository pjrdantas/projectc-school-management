package br.com.escola.aluno.application.port.out;

import java.util.UUID;

import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;

import br.com.escola.aluno.application.dto.AlunoOutput;

public interface AlunoQueryGateway {

    Optional<AlunoOutput> findById(@NonNull UUID id);

    List<AlunoOutput> findAll();

    boolean existsById(@NonNull UUID id);
}
