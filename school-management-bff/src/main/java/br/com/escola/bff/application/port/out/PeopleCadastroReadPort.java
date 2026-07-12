package br.com.escola.bff.application.port.out;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface PeopleCadastroReadPort {

    Mono<ResponseEntity<String>> consultarCadastro(
            CatalogReadQuery query,
            AuthSessionContext context,
            String nomeAluno,
            String cpfAluno,
            String nomeResponsavel,
            String cpfResponsavel,
            int page,
            int size);
}
