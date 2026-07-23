package br.com.escola.pedagogicalservice.application.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.port.in.DiarioClasseWriteUseCase;
import br.com.escola.pedagogicalservice.application.port.out.DiarioClasseWritePort;

@Service
public class DiarioClasseWriteService implements DiarioClasseWriteUseCase {

    private final DiarioClasseWritePort diarioClasseWritePort;

    public DiarioClasseWriteService(DiarioClasseWritePort diarioClasseWritePort) {
        this.diarioClasseWritePort = diarioClasseWritePort;
    }

    @Override
    public ResponseEntity<String> salvar(
            String authorization,
            InternalRequestContext context,
            String idDiarioClasse,
            String requestBody) {
        return diarioClasseWritePort.salvar(authorization, context, idDiarioClasse, requestBody);
    }
}
