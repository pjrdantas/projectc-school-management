package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AdministracaoAcessoCommand;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.in.AdministrarAcessoUseCase;
import br.com.escola.bff.application.port.out.AdministracaoAcessoPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import reactor.core.publisher.Mono;

public class AdministracaoAcessoProxyService implements AdministrarAcessoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final AdministracaoAcessoPort administracaoAcessoPort;

    public AdministracaoAcessoProxyService(
            InternalAuthContextPort authContextPort,
            AdministracaoAcessoPort administracaoAcessoPort) {
        this.authContextPort = authContextPort;
        this.administracaoAcessoPort = administracaoAcessoPort;
    }

    @Override
    public Mono<ResponseEntity<String>> executar(AdministracaoAcessoCommand command) {
        CatalogReadQuery query = new CatalogReadQuery(command.authorization(), command.correlationId());
        return authContextPort.resolve(query)
                .flatMap(context -> administracaoAcessoPort.executar(command, context));
    }
}
