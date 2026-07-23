package br.com.escola.bff.application.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.PainelWritePort;
import br.com.escola.bff.application.usecase.PainelWriteUseCase;
import reactor.core.publisher.Mono;

public class PainelWriteProxyService implements PainelWriteUseCase {

    private final AuthContextPort authContextPort;
    private final PainelWritePort painelWritePort;

    public PainelWriteProxyService(AuthContextPort authContextPort, PainelWritePort painelWritePort) {
        this.authContextPort = authContextPort;
        this.painelWritePort = painelWritePort;
    }

    @Override
    public Mono<ResponseEntity<String>> encaminhar(
            String authorization, String correlationId, HttpMethod method, String caminhoInterno, String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> painelWritePort.encaminhar(method, caminhoInterno, requestBody, query, context));
    }
}
