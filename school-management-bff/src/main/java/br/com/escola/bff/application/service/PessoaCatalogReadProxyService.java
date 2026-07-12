package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.PeopleCatalogReadPort;
import br.com.escola.bff.application.usecase.ConsultarPessoaCatalogoUseCase;
import reactor.core.publisher.Mono;

public class PessoaCatalogReadProxyService implements ConsultarPessoaCatalogoUseCase {

    private final AuthContextPort authContextPort;
    private final PeopleCatalogReadPort peopleCatalogReadPort;

    public PessoaCatalogReadProxyService(
            AuthContextPort authContextPort,
            PeopleCatalogReadPort peopleCatalogReadPort) {
        this.authContextPort = authContextPort;
        this.peopleCatalogReadPort = peopleCatalogReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarTiposPessoa(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peopleCatalogReadPort.listarTiposPessoa(query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> listarTiposEndereco(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peopleCatalogReadPort.listarTiposEndereco(query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> listarStatusAluno(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peopleCatalogReadPort.listarStatusAluno(query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> listarParentescos(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peopleCatalogReadPort.listarParentescos(query, context));
    }
}
