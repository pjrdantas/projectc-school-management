package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.PedagogicalBoletimReadPort;
import br.com.escola.bff.application.usecase.ConsultarBoletimUseCase;
import reactor.core.publisher.Mono;

public class BoletimReadProxyService implements ConsultarBoletimUseCase {

    private final AuthContextPort authContextPort;
    private final PedagogicalBoletimReadPort pedagogicalBoletimReadPort;

    public BoletimReadProxyService(
            AuthContextPort authContextPort,
            PedagogicalBoletimReadPort pedagogicalBoletimReadPort) {
        this.authContextPort = authContextPort;
        this.pedagogicalBoletimReadPort = pedagogicalBoletimReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultarBoletimPorMatricula(
            String authorization,
            String correlationId,
            UUID matriculaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalBoletimReadPort.consultarBoletimPorMatricula(
                        matriculaId,
                        query,
                        context));
    }
}
