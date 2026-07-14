package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.PlanningAiConteudoReadPort;
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoIaConteudoUseCase;
import reactor.core.publisher.Mono;

public class PlanejamentoIaConteudoReadProxyService implements ConsultarPlanejamentoIaConteudoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PlanningAiConteudoReadPort planningAiConteudoReadPort;

    public PlanejamentoIaConteudoReadProxyService(
            InternalAuthContextPort authContextPort,
            PlanningAiConteudoReadPort planningAiConteudoReadPort) {
        this.authContextPort = authContextPort;
        this.planningAiConteudoReadPort = planningAiConteudoReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarConteudos(
            String authorization,
            String correlationId,
            UUID planejamentoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> planningAiConteudoReadPort.listarConteudos(
                        planejamentoId,
                        query,
                        context));
    }
}
