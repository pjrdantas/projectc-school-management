package br.com.escola.bff.application.usecase;

import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.TurmaDisciplinaLinkCommand;
import br.com.escola.bff.application.dto.TurmaDisciplinaLinkedResult;
import reactor.core.publisher.Mono;

public interface LinkTurmaDisciplinaUseCase {

    Mono<TurmaDisciplinaLinkedResult> executar(CatalogWriteQuery query, TurmaDisciplinaLinkCommand command);
}
