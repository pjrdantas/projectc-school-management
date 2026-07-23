package br.com.escola.pedagogicalservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.BoletimResponse;
import br.com.escola.pedagogicalservice.application.port.in.BoletimQueryUseCase;
import br.com.escola.pedagogicalservice.application.port.out.BoletimReadPort;

@Service
public class BoletimQueryService implements BoletimQueryUseCase {

    private final BoletimReadPort boletimReadPort;

    public BoletimQueryService(BoletimReadPort boletimReadPort) {
        this.boletimReadPort = boletimReadPort;
    }

    @Override
    public BoletimResponse consultarBoletimPorMatricula(
            String authorization,
            InternalRequestContext context,
            UUID matriculaId) {
        return boletimReadPort.consultarBoletimPorMatricula(authorization, context, matriculaId);
    }

    @Override
    public List<BoletimResponse> listarFechamentosPorMatricula(
            String authorization,
            InternalRequestContext context,
            UUID matriculaId) {
        return boletimReadPort.listarFechamentosPorMatricula(authorization, context, matriculaId);
    }
}
