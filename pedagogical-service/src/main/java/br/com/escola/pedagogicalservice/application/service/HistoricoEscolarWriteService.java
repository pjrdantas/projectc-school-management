package br.com.escola.pedagogicalservice.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.port.in.HistoricoEscolarWriteUseCase;
import br.com.escola.pedagogicalservice.application.port.out.HistoricoEscolarWritePort;

@Service
public class HistoricoEscolarWriteService implements HistoricoEscolarWriteUseCase {

    private final HistoricoEscolarWritePort historicoEscolarWritePort;

    public HistoricoEscolarWriteService(HistoricoEscolarWritePort historicoEscolarWritePort) {
        this.historicoEscolarWritePort = historicoEscolarWritePort;
    }

    @Override
    public ResponseEntity<String> criar(String authorization, InternalRequestContext context, String requestBody) {
        return historicoEscolarWritePort.criar(authorization, context, requestBody);
    }

    @Override
    public ResponseEntity<String> atualizar(
            String authorization,
            InternalRequestContext context,
            UUID historicoEscolarId,
            String requestBody) {
        return historicoEscolarWritePort.atualizar(authorization, context, historicoEscolarId, requestBody);
    }

    @Override
    public void excluir(InternalRequestContext context, UUID historicoEscolarId) { historicoEscolarWritePort.excluir(context, historicoEscolarId); }
}
