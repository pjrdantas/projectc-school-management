package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface PlanejamentoBimestralReadPort {

    Mono<ResponseEntity<String>> listar(
            UUID professorTurmaDisciplinaId,
            UUID periodoAvaliativoId,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> buscarPorId(
            UUID planejamentoId,
            CatalogReadQuery query,
            AuthSessionContext context);
}
