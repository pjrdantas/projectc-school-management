package br.com.escola.studentmanagement.application.port.out;

import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;

import br.com.escola.studentmanagement.application.dto.AlunoOutput;

public interface AlunoQueryGateway {

    Optional<AlunoOutput> findById(@NonNull Long id);

    List<AlunoOutput> findAll();

    boolean existsById(@NonNull Long id);
}
