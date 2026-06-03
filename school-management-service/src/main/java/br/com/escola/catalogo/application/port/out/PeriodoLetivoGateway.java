package br.com.escola.catalogo.application.port.out;

import java.util.UUID;

import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;

import br.com.escola.catalogo.application.dto.PeriodoLetivoInput;
import br.com.escola.catalogo.application.dto.PeriodoLetivoOutput;

public interface PeriodoLetivoGateway {

    Optional<PeriodoLetivoOutput> findById(@NonNull UUID id);

    boolean existsById(@NonNull UUID id);

    PeriodoLetivoOutput save(PeriodoLetivoInput input);

    List<PeriodoLetivoOutput> findAll();
}
