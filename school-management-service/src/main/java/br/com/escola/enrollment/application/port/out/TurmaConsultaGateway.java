package br.com.escola.enrollment.application.port.out;

import java.util.UUID;

import java.util.Optional;

import org.springframework.lang.NonNull;

public interface TurmaConsultaGateway {

    boolean existsById(@NonNull UUID id);

    Optional<UUID> findPeriodoLetivoIdByTurmaId(@NonNull UUID turmaId);

    Optional<Integer> findCapacidadeByTurmaId(@NonNull UUID turmaId);

    Optional<UUID> findSerieIdByTurmaId(@NonNull UUID turmaId);

    Optional<Integer> findSerieOrdemByTurmaId(@NonNull UUID turmaId);
}
