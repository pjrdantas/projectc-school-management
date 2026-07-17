package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.PessoaCadastroReadPort;
import br.com.escola.bff.infra.config.CadastroPessoaClientProperties;
import reactor.core.publisher.Mono;

@Component
public class PessoaCadastroReadClient extends AbstractDownstreamClientSupport implements PessoaCadastroReadPort {

    private final WebClient webClient;
    private final CadastroPessoaClientProperties properties;

    public PessoaCadastroReadClient(
            @Qualifier("peopleServiceWebClient")
            WebClient peopleServiceWebClient,
            CadastroPessoaClientProperties properties) {
        this.webClient = peopleServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> buscarPessoaPorId(
            UUID pessoaId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return get("/internal/v1/pessoas/{pessoaId}", pessoaId, query, context);
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> buscarEnderecoPrincipal(
            UUID pessoaId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return get("/internal/v1/pessoas/{pessoaId}/endereco-principal", pessoaId, query, context);
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarEnderecos(
            UUID pessoaId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return get("/internal/v1/pessoas/{pessoaId}/enderecos", pessoaId, query, context);
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> buscarContato(
            UUID pessoaId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return get("/internal/v1/pessoas/{pessoaId}/contato", pessoaId, query, context);
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarDocumentos(
            UUID pessoaId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return get("/internal/v1/pessoas/{pessoaId}/documentos", pessoaId, query, context);
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> buscarDocumentoPorId(
            UUID documentoId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return get("/internal/v1/documentos/{documentoId}", documentoId, query, context);
    }

    private Mono<org.springframework.http.ResponseEntity<String>> get(
            String path,
            UUID resourceId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri(path, resourceId)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "People service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "People service indisponivel"));
    }
}


