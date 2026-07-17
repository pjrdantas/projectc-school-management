package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface MonolithHistoricoEscolarReadPort {

    Mono<ResponseEntity<String>> carregarNovo(
            UUID alunoId,
            UUID matriculaId,
            String modo,
            CatalogReadQuery query);

    Mono<ResponseEntity<String>> carregarParaEdicao(
            UUID historicoEscolarId,
            CatalogReadQuery query);
}
