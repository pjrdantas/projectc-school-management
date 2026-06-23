package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.SerieCreateCommand;
import br.com.escola.bff.application.dto.SerieCreatedResult;
import reactor.core.publisher.Mono;

public interface MonolithSerieWritePort {

    Mono<SerieCreatedResult> criar(CatalogWriteQuery query, SerieCreateCommand command);
}
