package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface PedagogicalHistoricoEscolarReadPort {

    Mono<ResponseEntity<String>> carregarNovo(
            UUID alunoId,
            UUID matriculaId,
            String modo,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> carregarParaEdicao(
            UUID historicoEscolarId,
            CatalogReadQuery query,
            AuthSessionContext context);
}
