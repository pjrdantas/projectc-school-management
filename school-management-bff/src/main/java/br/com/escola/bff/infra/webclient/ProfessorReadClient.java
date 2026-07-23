package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.ProfessorReadPort;
import br.com.escola.bff.infra.config.ProfessorServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class ProfessorReadClient extends AbstractDownstreamClientSupport implements ProfessorReadPort {

    private final WebClient webClient;
    private final ProfessorServiceClientProperties properties;

    public ProfessorReadClient(
            @Qualifier("academicProfessorServiceWebClient")
            WebClient academicProfessorServiceWebClient,
            ProfessorServiceClientProperties properties) {
        this.webClient = academicProfessorServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarProfessores(
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/professores")
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Academic professor service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Academic professor service indisponivel"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> buscarProfessorPorId(
            UUID professorId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/professores/{professorId}", professorId)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Academic professor service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Academic professor service indisponivel"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarAlocacoesPorProfessor(
            UUID professorId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/professores/{professorId}/turmas-disciplinas", professorId)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Academic professor service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Academic professor service indisponivel"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarProfessoresPorTurma(
            UUID turmaId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/turmas/{turmaId}/professores", turmaId)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Academic professor service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Academic professor service indisponivel"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarFuncionariosElegiveis(
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/professores/funcionarios-elegiveis")
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Academic professor service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Academic professor service indisponivel"));
    }
}
