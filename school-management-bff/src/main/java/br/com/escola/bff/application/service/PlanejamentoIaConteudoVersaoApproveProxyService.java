package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.PlanningAiConteudoVersaoApprovePort;
import br.com.escola.bff.application.usecase.AprovarPlanejamentoIaConteudoVersaoUseCase;
import reactor.core.publisher.Mono;

public class PlanejamentoIaConteudoVersaoApproveProxyService implements AprovarPlanejamentoIaConteudoVersaoUseCase {

    private final AuthContextPort authContextPort;
    private final PlanningAiConteudoVersaoApprovePort planningAiConteudoVersaoApprovePort;

    public PlanejamentoIaConteudoVersaoApproveProxyService(
            AuthContextPort authContextPort,
            PlanningAiConteudoVersaoApprovePort planningAiConteudoVersaoApprovePort) {
        this.authContextPort = authContextPort;
        this.planningAiConteudoVersaoApprovePort = planningAiConteudoVersaoApprovePort;
    }

    @Override
    public Mono<ResponseEntity<String>> executar(
            String authorization,
            String correlationId,
            UUID conteudoId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> planningAiConteudoVersaoApprovePort.aprovarVersao(
                        conteudoId,
                        requestBody,
                        query,
                        context));
    }
}
