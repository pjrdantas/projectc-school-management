package br.com.escola.bff.application.port.out;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface PedagogicalDiarioClasseReadPort {

    Mono<ResponseEntity<String>> carregar(
            UUID professorId,
            UUID turmaId,
            UUID disciplinaId,
            Integer anoLetivo,
            Integer mes,
            LocalDate dataReferencia,
            CatalogReadQuery query,
            AuthSessionContext context);
}
