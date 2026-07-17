package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.FuncionarioCadastroReadPort;
import br.com.escola.bff.application.usecase.ConsultarFuncionarioUseCase;
import reactor.core.publisher.Mono;

public class FuncionarioReadProxyService implements ConsultarFuncionarioUseCase {

    private final InternalAuthContextPort authContextPort;
    private final FuncionarioCadastroReadPort peopleFuncionarioReadPort;

    public FuncionarioReadProxyService(
            InternalAuthContextPort authContextPort,
            FuncionarioCadastroReadPort peopleFuncionarioReadPort) {
        this.authContextPort = authContextPort;
        this.peopleFuncionarioReadPort = peopleFuncionarioReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarFuncionarios(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peopleFuncionarioReadPort.listarFuncionarios(query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarFuncionarioPorId(
            String authorization,
            String correlationId,
            UUID funcionarioId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peopleFuncionarioReadPort.buscarFuncionarioPorId(funcionarioId, query, context));
    }
}

