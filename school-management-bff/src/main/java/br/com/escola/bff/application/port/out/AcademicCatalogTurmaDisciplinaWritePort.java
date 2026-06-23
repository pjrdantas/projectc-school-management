package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.TurmaDisciplinaLinkCommand;
import br.com.escola.bff.application.dto.TurmaDisciplinaLinkedResult;
import reactor.core.publisher.Mono;

public interface AcademicCatalogTurmaDisciplinaWritePort {

    Mono<TurmaDisciplinaLinkedResult> vincular(
            CatalogWriteQuery query,
            AuthSessionContext context,
            TurmaDisciplinaLinkCommand command);
}
