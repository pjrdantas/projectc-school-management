package br.com.escola.bff.application.port.out;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface PainelWritePort {

    Mono<ResponseEntity<String>> encaminhar(
            HttpMethod method, String caminhoInterno, String requestBody, CatalogReadQuery query, AuthSessionContext context);
}
