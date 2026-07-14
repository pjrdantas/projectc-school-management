package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.PlanningAiConteudoDetailReadPort;
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoIaConteudoDetailUseCase;
import reactor.core.publisher.Mono;

public class PlanejamentoIaConteudoDetailReadProxyService implements ConsultarPlanejamentoIaConteudoDetailUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PlanningAiConteudoDetailReadPort planningAiConteudoDetailReadPort;

    public PlanejamentoIaConteudoDetailReadProxyService(
            InternalAuthContextPort authContextPort,
            PlanningAiConteudoDetailReadPort planningAiConteudoDetailReadPort) {
        this.authContextPort = authContextPort;
        this.planningAiConteudoDetailReadPort = planningAiConteudoDetailReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> buscarConteudo(
            String authorization,
            String correlationId,
            UUID conteudoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> planningAiConteudoDetailReadPort.buscarConteudo(
                        conteudoId,
                        query,
                        context));
    }
}
