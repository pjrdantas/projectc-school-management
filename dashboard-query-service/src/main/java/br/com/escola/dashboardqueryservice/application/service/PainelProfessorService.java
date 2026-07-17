package br.com.escola.dashboardqueryservice.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelProfessorResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelProfessorUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.PainelProfessorPort;

@Service
public class PainelProfessorService implements PainelProfessorUseCase {

    private final PainelProfessorPort dashboardProfessorPort;

    public PainelProfessorService(PainelProfessorPort dashboardProfessorPort) {
        this.dashboardProfessorPort = dashboardProfessorPort;
    }

    @Override
    public PainelProfessorResponse consultar(String authorization, InternalRequestContext context, UUID professorId) {
        return dashboardProfessorPort.consultar(authorization, context, professorId);
    }
}

