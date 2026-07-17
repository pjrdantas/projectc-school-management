package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.ResponsavelCatalogoReadPort;
import br.com.escola.bff.application.usecase.ConsultarResponsavelUseCase;
import reactor.core.publisher.Mono;

public class ResponsavelReadProxyService implements ConsultarResponsavelUseCase {

    private final InternalAuthContextPort authContextPort;
    private final ResponsavelCatalogoReadPort responsiblesReadPort;

    public ResponsavelReadProxyService(
            InternalAuthContextPort authContextPort,
            ResponsavelCatalogoReadPort responsiblesReadPort) {
        this.authContextPort = authContextPort;
        this.responsiblesReadPort = responsiblesReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarResponsaveis(
            String authorization,
            String correlationId,
            String nome,
            String cpf) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> responsiblesReadPort.listarResponsaveis(nome, cpf, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarResponsavelPorId(
            String authorization,
            String correlationId,
            UUID responsavelId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> responsiblesReadPort.buscarResponsavelPorId(responsavelId, query, context));
    }
}
