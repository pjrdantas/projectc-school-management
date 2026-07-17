package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.PeriodoLetivoCreateCommand;
import br.com.escola.bff.application.dto.PeriodoLetivoCreatedResult;
import reactor.core.publisher.Mono;

public interface LegacyPeriodoLetivoWritePort {

    Mono<PeriodoLetivoCreatedResult> criar(CatalogWriteQuery query, PeriodoLetivoCreateCommand command);
}

