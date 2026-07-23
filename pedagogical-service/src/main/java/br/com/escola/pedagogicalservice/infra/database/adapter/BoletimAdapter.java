package br.com.escola.pedagogicalservice.infra.database.adapter;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.BoletimResponse;
import br.com.escola.pedagogicalservice.application.port.out.BoletimReadPort;

@Repository
public class BoletimAdapter implements BoletimReadPort {

    private final PersistenciaLocalAdapter persistenciaLocalAdapter;

    public BoletimAdapter(PersistenciaLocalAdapter persistenciaLocalAdapter) {
        this.persistenciaLocalAdapter = persistenciaLocalAdapter;
    }

    @Override
    public BoletimResponse consultarBoletimPorMatricula(String authorization, InternalRequestContext context, UUID matriculaId) {
        return persistenciaLocalAdapter.consultarBoletimPorMatricula(authorization, context, matriculaId);
    }

    @Override
    public List<BoletimResponse> listarFechamentosPorMatricula(String authorization, InternalRequestContext context, UUID matriculaId) {
        return persistenciaLocalAdapter.listarFechamentosPorMatricula(authorization, context, matriculaId);
    }
}
