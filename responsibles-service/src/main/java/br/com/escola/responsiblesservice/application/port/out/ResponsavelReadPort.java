package br.com.escola.responsiblesservice.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.responsiblesservice.application.context.InternalRequestContext;

public interface ResponsavelReadPort {

    ResponseEntity<String> buscarResponsavelPorId(
            String authorization,
            InternalRequestContext context,
            UUID responsavelId);

    ResponseEntity<String> listarResponsaveisPorAluno(
            String authorization,
            InternalRequestContext context,
            UUID alunoId);
}
