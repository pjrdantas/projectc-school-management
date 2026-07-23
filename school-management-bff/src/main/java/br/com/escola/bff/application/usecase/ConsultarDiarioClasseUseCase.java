package br.com.escola.bff.application.usecase;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarDiarioClasseUseCase {

    Mono<ResponseEntity<String>> carregar(
            String authorization,
            String correlationId,
            UUID professorId,
            UUID turmaId,
            UUID disciplinaId,
            Integer anoLetivo,
            Integer mes,
            LocalDate dataReferencia);
}
