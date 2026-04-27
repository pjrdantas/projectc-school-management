package br.com.escola.enrollment.application.port.out;

import java.util.Optional;

import org.springframework.lang.NonNull;

public interface TurmaConsultaGateway {

    boolean existsById(@NonNull Long id);

    Optional<Long> findPeriodoLetivoIdByTurmaId(@NonNull Long turmaId);
}
