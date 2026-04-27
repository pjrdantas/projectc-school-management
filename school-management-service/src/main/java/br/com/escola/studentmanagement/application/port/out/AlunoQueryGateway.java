package br.com.escola.studentmanagement.application.port.out;

import java.util.Optional;

import org.springframework.lang.NonNull;

import br.com.escola.studentmanagement.application.dto.AlunoOutput;

public interface AlunoQueryGateway {

    Optional<AlunoOutput> findById(@NonNull Long id);

    boolean existsById(@NonNull Long id);
}
