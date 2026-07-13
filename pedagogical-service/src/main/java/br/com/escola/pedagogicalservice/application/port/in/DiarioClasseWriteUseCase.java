package br.com.escola.pedagogicalservice.application.port.in;

import org.springframework.http.ResponseEntity;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;

public interface DiarioClasseWriteUseCase {

    ResponseEntity<String> salvar(
            String authorization,
            InternalRequestContext context,
            String idDiarioClasse,
            String requestBody);
}
