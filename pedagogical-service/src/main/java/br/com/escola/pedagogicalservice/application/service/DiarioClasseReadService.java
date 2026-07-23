package br.com.escola.pedagogicalservice.application.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.port.in.DiarioClasseReadUseCase;
import br.com.escola.pedagogicalservice.application.port.out.DiarioClasseReadPort;

@Service
public class DiarioClasseReadService implements DiarioClasseReadUseCase {

    private final DiarioClasseReadPort diarioClasseReadPort;

    public DiarioClasseReadService(DiarioClasseReadPort diarioClasseReadPort) {
        this.diarioClasseReadPort = diarioClasseReadPort;
    }

    @Override
    public ResponseEntity<String> carregar(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            UUID turmaId,
            UUID disciplinaId,
            Integer anoLetivo,
            Integer mes,
            LocalDate dataReferencia) {
        return diarioClasseReadPort.carregar(
                authorization,
                context,
                professorId,
                turmaId,
                disciplinaId,
                anoLetivo,
                mes,
                dataReferencia);
    }
}
