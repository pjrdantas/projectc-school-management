package br.com.escola.bff.application.port.out;

import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.databind.JsonNode;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import reactor.core.publisher.Mono;

public interface CatalogoMutationWritePort {

    Mono<MutationResponse> executar(
            HttpMethod method,
            String path,
            Object body,
            CatalogWriteQuery query,
            AuthSessionContext context);

    record MutationResponse(JsonNode body, boolean replayed) {
    }
}
