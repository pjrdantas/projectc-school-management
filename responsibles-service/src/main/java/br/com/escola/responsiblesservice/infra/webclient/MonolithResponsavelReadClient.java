package br.com.escola.responsiblesservice.infra.webclient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import br.com.escola.responsiblesservice.application.context.InternalRequestContext;
import br.com.escola.responsiblesservice.application.port.out.ResponsavelReadPort;

@Component
public class MonolithResponsavelReadClient implements ResponsavelReadPort {

    private final RestClient restClient;

    public MonolithResponsavelReadClient(RestClient responsiblesMonolithRestClient) {
        this.restClient = responsiblesMonolithRestClient;
    }

    @Override
    public ResponseEntity<String> buscarResponsavelPorId(
            String authorization,
            InternalRequestContext context,
            UUID responsavelId) {
        return withHeaders(restClient.get().uri("/api/responsaveis/{id}", responsavelId), authorization, context)
                .exchange((request, response) -> ResponseEntity.status(response.getStatusCode())
                        .headers(headers -> {
                            MediaType contentType = response.getHeaders().getContentType();
                            if (contentType != null) {
                                headers.setContentType(contentType);
                            }
                        })
                        .body(readBody(response)));
    }

    private RestClient.RequestHeadersSpec<?> withHeaders(
            RestClient.RequestHeadersSpec<?> spec,
            String authorization,
            InternalRequestContext context) {
        return spec.headers(headers -> {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
            headers.set("X-Correlation-Id", context.correlationId());
            headers.set("X-Usuario-Id", context.usuarioId().toString());
            headers.set("X-Escola-Id", context.escolaId().toString());
        });
    }

    private String readBody(org.springframework.http.client.ClientHttpResponse response) throws IOException {
        return StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
    }
}
