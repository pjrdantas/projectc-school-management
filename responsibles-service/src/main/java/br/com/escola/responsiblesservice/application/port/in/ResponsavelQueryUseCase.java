package br.com.escola.responsiblesservice.application.port.in;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.responsiblesservice.application.context.InternalRequestContext;

public interface ResponsavelQueryUseCase {

    ResponseEntity<String> buscarResponsavelPorId(
            String authorization,
            InternalRequestContext context,
            UUID responsavelId);

    ResponseEntity<String> listarResponsaveisPorAluno(
            String authorization,
            InternalRequestContext context,
            UUID alunoId);
}
