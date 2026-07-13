package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface PlanningAiConteudoVersaoApprovePort {

    Mono<ResponseEntity<String>> aprovarVersao(
            UUID conteudoId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context);
}
