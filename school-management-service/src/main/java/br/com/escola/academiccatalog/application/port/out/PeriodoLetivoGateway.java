package br.com.escola.academiccatalog.application.port.out;

import java.util.UUID;

import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;

import br.com.escola.academiccatalog.application.dto.PeriodoLetivoInput;
import br.com.escola.academiccatalog.application.dto.PeriodoLetivoOutput;

public interface PeriodoLetivoGateway {

    Optional<PeriodoLetivoOutput> findById(@NonNull UUID id);

    boolean existsById(@NonNull UUID id);

    PeriodoLetivoOutput save(PeriodoLetivoInput input);

    List<PeriodoLetivoOutput> findAll();
}
