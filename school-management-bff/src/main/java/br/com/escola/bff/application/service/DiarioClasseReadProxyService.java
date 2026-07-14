package br.com.escola.bff.application.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.PedagogicalDiarioClasseReadPort;
import br.com.escola.bff.application.usecase.ConsultarDiarioClasseUseCase;
import reactor.core.publisher.Mono;

public class DiarioClasseReadProxyService implements ConsultarDiarioClasseUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PedagogicalDiarioClasseReadPort pedagogicalDiarioClasseReadPort;

    public DiarioClasseReadProxyService(
            InternalAuthContextPort authContextPort,
            PedagogicalDiarioClasseReadPort pedagogicalDiarioClasseReadPort) {
        this.authContextPort = authContextPort;
        this.pedagogicalDiarioClasseReadPort = pedagogicalDiarioClasseReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> carregar(
            String authorization,
            String correlationId,
            UUID professorId,
            UUID turmaId,
            UUID disciplinaId,
            Integer anoLetivo,
            Integer mes,
            LocalDate dataReferencia) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalDiarioClasseReadPort.carregar(
                        professorId,
                        turmaId,
                        disciplinaId,
                        anoLetivo,
                        mes,
                        dataReferencia,
                        query,
                        context));
    }
}
