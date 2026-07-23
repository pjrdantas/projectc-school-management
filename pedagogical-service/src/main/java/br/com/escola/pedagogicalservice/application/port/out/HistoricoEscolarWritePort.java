package br.com.escola.pedagogicalservice.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;

public interface HistoricoEscolarWritePort {

    ResponseEntity<String> criar(
            String authorization,
            InternalRequestContext context,
            String requestBody);

    ResponseEntity<String> atualizar(
            String authorization,
            InternalRequestContext context,
            UUID historicoEscolarId,
            String requestBody);

    void excluir(InternalRequestContext context, UUID historicoEscolarId);
}
