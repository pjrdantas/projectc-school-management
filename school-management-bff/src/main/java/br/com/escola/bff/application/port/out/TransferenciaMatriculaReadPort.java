package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface TransferenciaMatriculaReadPort {

    Mono<ResponseEntity<String>> listarTransferenciasPorAluno(
            UUID alunoId,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> buscarTransferenciaPorId(
            UUID transferenciaId,
            CatalogReadQuery query,
            AuthSessionContext context);
}

