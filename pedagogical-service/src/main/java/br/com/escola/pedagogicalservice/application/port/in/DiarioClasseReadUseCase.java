package br.com.escola.pedagogicalservice.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;

public interface DiarioClasseReadUseCase {

    ResponseEntity<String> carregar(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            UUID turmaId,
            UUID disciplinaId,
            Integer anoLetivo,
            Integer mes,
            LocalDate dataReferencia);
}
