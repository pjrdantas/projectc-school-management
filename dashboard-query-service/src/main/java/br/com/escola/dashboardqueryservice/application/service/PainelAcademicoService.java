package br.com.escola.dashboardqueryservice.application.service;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelAcademicoResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelAcademicoUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.PainelAcademicoPort;

@Service
public class PainelAcademicoService implements PainelAcademicoUseCase {

    private final PainelAcademicoPort dashboardAcademicoPort;

    public PainelAcademicoService(PainelAcademicoPort dashboardAcademicoPort) {
        this.dashboardAcademicoPort = dashboardAcademicoPort;
    }

    @Override
    public PainelAcademicoResponse consultar(String authorization, InternalRequestContext context) {
        return dashboardAcademicoPort.consultarAcademico(authorization, context);
    }
}

