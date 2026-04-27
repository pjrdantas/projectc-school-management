package br.com.escola.academiccatalog.application.port.out;

import java.util.Optional;

import org.springframework.lang.NonNull;

import br.com.escola.academiccatalog.application.dto.PeriodoLetivoInput;
import br.com.escola.academiccatalog.application.dto.PeriodoLetivoOutput;

public interface PeriodoLetivoGateway {

    Optional<PeriodoLetivoOutput> findById(@NonNull Long id);

    boolean existsById(@NonNull Long id);

    PeriodoLetivoOutput save(PeriodoLetivoInput input);
}
