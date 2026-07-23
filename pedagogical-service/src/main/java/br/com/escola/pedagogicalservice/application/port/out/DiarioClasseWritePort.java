package br.com.escola.pedagogicalservice.application.port.out;

import org.springframework.http.ResponseEntity;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;

public interface DiarioClasseWritePort {

    ResponseEntity<String> salvar(
            String authorization,
            InternalRequestContext context,
            String idDiarioClasse,
            String requestBody);
}
