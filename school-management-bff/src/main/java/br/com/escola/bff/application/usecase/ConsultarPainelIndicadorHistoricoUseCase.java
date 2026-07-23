package br.com.escola.bff.application.usecase;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarPainelIndicadorHistoricoUseCase {

    Mono<ResponseEntity<String>> consultarHistorico(
            String authorization,
            String correlationId,
            String publicoCodigo,
            String codigoIndicador,
            LocalDate dataInicio,
            LocalDate dataFim,
            UUID professorId);
}

