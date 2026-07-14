package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.PlanningAiBibliotecaReadPort;
import br.com.escola.bff.application.usecase.ConsultarBibliotecaConteudoPedagogicoUseCase;
import reactor.core.publisher.Mono;

public class BibliotecaConteudoPedagogicoReadProxyService implements ConsultarBibliotecaConteudoPedagogicoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PlanningAiBibliotecaReadPort planningAiBibliotecaReadPort;

    public BibliotecaConteudoPedagogicoReadProxyService(
            InternalAuthContextPort authContextPort,
            PlanningAiBibliotecaReadPort planningAiBibliotecaReadPort) {
        this.authContextPort = authContextPort;
        this.planningAiBibliotecaReadPort = planningAiBibliotecaReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarBiblioteca(
            String authorization,
            String correlationId,
            UUID professorId,
            UUID disciplinaId,
            String tipoConteudo,
            String tema) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> planningAiBibliotecaReadPort.listarBiblioteca(
                        professorId,
                        disciplinaId,
                        tipoConteudo,
                        tema,
                        query,
                        context));
    }
}
