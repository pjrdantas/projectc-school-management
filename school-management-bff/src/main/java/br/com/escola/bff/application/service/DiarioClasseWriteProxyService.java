package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.PedagogicalDiarioClasseWritePort;
import br.com.escola.bff.application.usecase.SalvarDiarioClasseUseCase;
import reactor.core.publisher.Mono;

public class DiarioClasseWriteProxyService implements SalvarDiarioClasseUseCase {

    private final AuthContextPort authContextPort;
    private final PedagogicalDiarioClasseWritePort pedagogicalDiarioClasseWritePort;

    public DiarioClasseWriteProxyService(
            AuthContextPort authContextPort,
            PedagogicalDiarioClasseWritePort pedagogicalDiarioClasseWritePort) {
        this.authContextPort = authContextPort;
        this.pedagogicalDiarioClasseWritePort = pedagogicalDiarioClasseWritePort;
    }

    @Override
    public Mono<ResponseEntity<String>> salvar(
            String authorization,
            String correlationId,
            String idDiarioClasse,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalDiarioClasseWritePort.salvar(idDiarioClasse, requestBody, query, context));
    }
}
