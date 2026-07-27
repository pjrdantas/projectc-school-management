package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.HistoricoEscolarWritePort;
import br.com.escola.bff.infra.config.EnsinoClientProperties;
import reactor.core.publisher.Mono;

@Component
public class HistoricoEscolarWriteClient extends AbstractDownstreamClientSupport
        implements HistoricoEscolarWritePort {

    private final WebClient webClient;
    private final EnsinoClientProperties properties;

    public HistoricoEscolarWriteClient(
            @Qualifier("pedagogicalServiceWebClient") WebClient pedagogicalServiceWebClient,
            EnsinoClientProperties properties) {
        this.webClient = pedagogicalServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<ResponseEntity<String>> importarPdf(FilePart arquivo, CatalogReadQuery query, AuthSessionContext context) {
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        MediaType contentType = arquivo.headers().getContentType();
        body.asyncPart("arquivo", arquivo.content(), DataBuffer.class)
                .filename(arquivo.filename())
                .contentType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM : contentType);
        return webClient.post()
                .uri("/internal/v1/historicos-escolares/importacao-pdf")
                .headers(headers -> {
                    headers.set(HttpHeaders.AUTHORIZATION, query.authorization());
                    headers.set("X-Internal-Token", properties.internalToken());
                    headers.set("X-Correlation-Id", query.correlationId());
                    headers.set("X-Usuario-Id", context.usuarioId().toString());
                    headers.set("X-Escola-Id", context.escolaId().toString());
                })
                .body(BodyInserters.fromMultipartData(body.build()))
                .exchangeToMono(response -> handle(response, "Pedagogical service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Pedagogical service indisponivel"));
    }

    @Override
    public Mono<ResponseEntity<String>> criar(String requestBody, CatalogReadQuery query, AuthSessionContext context) {
        return webClient.post()
                .uri("/internal/v1/historicos-escolares")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .bodyValue(requestBody)
                .exchangeToMono(response -> handle(response, "Pedagogical service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Pedagogical service indisponivel"));
    }

    @Override
    public Mono<ResponseEntity<String>> atualizar(
            UUID historicoEscolarId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.put()
                .uri("/internal/v1/historicos-escolares/{id}", historicoEscolarId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .bodyValue(requestBody)
                .exchangeToMono(response -> handle(response, "Pedagogical service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Pedagogical service indisponivel"));
    }

    @Override
    public Mono<ResponseEntity<String>> excluir(
            UUID historicoEscolarId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.delete()
                .uri("/internal/v1/historicos-escolares/{id}", historicoEscolarId)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Pedagogical service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Pedagogical service indisponivel"));
    }
}


