package br.com.escola.bff.application.port.in;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AdministracaoAcessoCommand;
import reactor.core.publisher.Mono;

public interface AdministrarAcessoUseCase {

    Mono<ResponseEntity<String>> executar(AdministracaoAcessoCommand command);
}
