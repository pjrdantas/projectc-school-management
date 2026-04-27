package br.com.escola.academiccatalog.application.port.out;

import java.util.Optional;

import org.springframework.lang.NonNull;

import br.com.escola.academiccatalog.application.dto.TurmaInput;
import br.com.escola.academiccatalog.application.dto.TurmaOutput;

public interface TurmaGateway {

    Optional<TurmaOutput> findById(@NonNull Long id);

    Optional<TurmaOutput> findByCodigoAndPeriodoLetivoId(String codigo, Long periodoLetivoId);

    TurmaOutput save(TurmaInput input);
}
