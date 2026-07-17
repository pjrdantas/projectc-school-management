package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.PessoaCadastroReadPort;
import br.com.escola.bff.application.usecase.ConsultarPessoaDetalheUseCase;
import reactor.core.publisher.Mono;

public class PessoaDetailReadProxyService implements ConsultarPessoaDetalheUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PessoaCadastroReadPort peoplePessoaReadPort;

    public PessoaDetailReadProxyService(
            InternalAuthContextPort authContextPort,
            PessoaCadastroReadPort peoplePessoaReadPort) {
        this.authContextPort = authContextPort;
        this.peoplePessoaReadPort = peoplePessoaReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> buscarPessoaPorId(String authorization, String correlationId, UUID pessoaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peoplePessoaReadPort.buscarPessoaPorId(pessoaId, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarEnderecoPrincipal(String authorization, String correlationId, UUID pessoaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peoplePessoaReadPort.buscarEnderecoPrincipal(pessoaId, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> listarEnderecos(String authorization, String correlationId, UUID pessoaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peoplePessoaReadPort.listarEnderecos(pessoaId, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarContato(String authorization, String correlationId, UUID pessoaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peoplePessoaReadPort.buscarContato(pessoaId, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> listarDocumentos(String authorization, String correlationId, UUID pessoaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peoplePessoaReadPort.listarDocumentos(pessoaId, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarDocumentoPorId(String authorization, String correlationId, UUID documentoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peoplePessoaReadPort.buscarDocumentoPorId(documentoId, query, context));
    }
}

