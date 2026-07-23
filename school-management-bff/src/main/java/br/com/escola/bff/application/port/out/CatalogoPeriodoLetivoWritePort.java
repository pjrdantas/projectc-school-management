package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.PeriodoLetivoCreateCommand;
import br.com.escola.bff.application.dto.PeriodoLetivoCreatedResult;
import reactor.core.publisher.Mono;

public interface CatalogoPeriodoLetivoWritePort {

    Mono<PeriodoLetivoCreatedResult> criar(
            CatalogWriteQuery query,
            AuthSessionContext context,
            PeriodoLetivoCreateCommand command);
}

