package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AlunoReadPort;
import br.com.escola.bff.infra.config.CadastroPessoaClientProperties;
import reactor.core.publisher.Mono;

@Component
public class AlunoReadClient extends AbstractDownstreamClientSupport implements AlunoReadPort {

    private final WebClient webClient;
    private final CadastroPessoaClientProperties properties;

    public AlunoReadClient(
            @Qualifier("peopleServiceWebClient") WebClient peopleServiceWebClient,
            CadastroPessoaClientProperties properties) {
        this.webClient = peopleServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<ResponseEntity<String>> listar(String nome, CatalogReadQuery query, AuthSessionContext context) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/internal/v1/alunos");
                    if (StringUtils.hasText(nome)) {
                        builder.queryParam("nome", nome);
                    }
                    return builder.build();
                })
                .headers(headers -> applyInternalHeaders(headers, query, context))
                .exchangeToMono(response -> handle(response, "People service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "People service indisponivel"));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarPorId(UUID alunoId, CatalogReadQuery query, AuthSessionContext context) {
        return get("/internal/v1/alunos/{alunoId}", alunoId, query, context);
    }

    @Override
    public Mono<ResponseEntity<String>> buscarFicha(UUID alunoId, CatalogReadQuery query, AuthSessionContext context) {
        return get("/internal/v1/alunos/{alunoId}/ficha", alunoId, query, context);
    }

    private Mono<ResponseEntity<String>> get(
            String path,
            UUID alunoId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri(path, alunoId)
                .headers(headers -> applyInternalHeaders(headers, query, context))
                .exchangeToMono(response -> handle(response, "People service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "People service indisponivel"));
    }

    private void applyInternalHeaders(
            org.springframework.http.HttpHeaders headers,
            CatalogReadQuery query,
            AuthSessionContext context) {
        headers.set("Authorization", query.authorization());
        headers.set("X-Internal-Token", properties.internalToken());
        headers.set("X-Correlation-Id", query.correlationId());
        headers.set("X-Usuario-Id", context.usuarioId().toString());
        headers.set("X-Escola-Id", context.escolaId().toString());
    }
}
