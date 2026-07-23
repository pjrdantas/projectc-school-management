package br.com.escola.bff.application.port.out;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AdministracaoAcessoCommand;
import br.com.escola.bff.application.dto.AuthSessionContext;
import reactor.core.publisher.Mono;

public interface AdministracaoAcessoPort {

    Mono<ResponseEntity<String>> executar(
            AdministracaoAcessoCommand command,
            AuthSessionContext context);
}
