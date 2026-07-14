package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.PlanningAiConteudoVersaoReadPort;
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoIaConteudoVersaoUseCase;
import reactor.core.publisher.Mono;

public class PlanejamentoIaConteudoVersaoReadProxyService implements ConsultarPlanejamentoIaConteudoVersaoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PlanningAiConteudoVersaoReadPort planningAiConteudoVersaoReadPort;

    public PlanejamentoIaConteudoVersaoReadProxyService(
            InternalAuthContextPort authContextPort,
            PlanningAiConteudoVersaoReadPort planningAiConteudoVersaoReadPort) {
        this.authContextPort = authContextPort;
        this.planningAiConteudoVersaoReadPort = planningAiConteudoVersaoReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarVersoes(
            String authorization,
            String correlationId,
            UUID conteudoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> planningAiConteudoVersaoReadPort.listarVersoes(
                        conteudoId,
                        query,
                        context));
    }
}
