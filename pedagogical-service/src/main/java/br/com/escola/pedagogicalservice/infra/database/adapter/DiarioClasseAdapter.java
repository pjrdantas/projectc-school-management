package br.com.escola.pedagogicalservice.infra.database.adapter;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.port.out.DiarioClasseReadPort;
import br.com.escola.pedagogicalservice.application.port.out.DiarioClasseWritePort;

@Repository
public class DiarioClasseAdapter implements DiarioClasseReadPort, DiarioClasseWritePort {

    private final PersistenciaLocalAdapter persistenciaLocalAdapter;

    public DiarioClasseAdapter(PersistenciaLocalAdapter persistenciaLocalAdapter) {
        this.persistenciaLocalAdapter = persistenciaLocalAdapter;
    }

    @Override
    public ResponseEntity<String> carregar(String authorization, InternalRequestContext context, UUID professorId, UUID turmaId, UUID disciplinaId, Integer anoLetivo, Integer mes, LocalDate dataReferencia) {
        return persistenciaLocalAdapter.carregarDiarioClasse(
                authorization,
                context,
                professorId,
                turmaId,
                disciplinaId,
                anoLetivo,
                mes,
                dataReferencia);
    }

    @Override
    public ResponseEntity<String> salvar(String authorization, InternalRequestContext context, String idDiarioClasse, String requestBody) {
        return persistenciaLocalAdapter.salvarDiarioClasse(authorization, context, idDiarioClasse, requestBody);
    }
}
