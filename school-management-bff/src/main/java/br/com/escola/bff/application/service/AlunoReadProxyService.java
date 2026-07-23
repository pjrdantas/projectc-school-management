package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AlunoReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.usecase.ConsultarAlunoUseCase;
import reactor.core.publisher.Mono;

public class AlunoReadProxyService implements ConsultarAlunoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final AlunoReadPort peopleAlunoReadPort;

    public AlunoReadProxyService(
            InternalAuthContextPort authContextPort,
            AlunoReadPort peopleAlunoReadPort) {
        this.authContextPort = authContextPort;
        this.peopleAlunoReadPort = peopleAlunoReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listar(String authorization, String correlationId, String nome) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peopleAlunoReadPort.listar(nome, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarPorId(String authorization, String correlationId, UUID alunoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peopleAlunoReadPort.buscarPorId(alunoId, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarFicha(String authorization, String correlationId, UUID alunoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peopleAlunoReadPort.buscarFicha(alunoId, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> encaminharEscrita(
            HttpMethod method, UUID alunoId, String body, String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peopleAlunoReadPort.encaminharEscrita(method, alunoId, body, query, context));
    }
}
