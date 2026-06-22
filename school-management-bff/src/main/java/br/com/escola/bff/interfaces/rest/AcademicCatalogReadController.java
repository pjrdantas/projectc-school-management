package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.service.CatalogReadRoute;
import br.com.escola.bff.application.usecase.RouteCatalogReadUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.catalog-read-proxy-enabled", havingValue = "true")
public class AcademicCatalogReadController {

    private final RouteCatalogReadUseCase routeCatalogReadUseCase;

    public AcademicCatalogReadController(RouteCatalogReadUseCase routeCatalogReadUseCase) {
        this.routeCatalogReadUseCase = routeCatalogReadUseCase;
    }

    @GetMapping("/api/disciplinas")
    public Mono<ResponseEntity<String>> listarDisciplinas(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return executar(CatalogReadRoute.DISCIPLINAS, authorization, correlationId);
    }

    @GetMapping("/api/disciplinas/{id}")
    public Mono<ResponseEntity<String>> buscarDisciplina(
            @PathVariable UUID id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return executar(CatalogReadRoute.DISCIPLINA_POR_ID, authorization, correlationId, id.toString());
    }

    @GetMapping("/api/periodos-letivos")
    public Mono<ResponseEntity<String>> listarPeriodosLetivos(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return executar(CatalogReadRoute.PERIODOS_LETIVOS, authorization, correlationId);
    }

    @GetMapping("/api/periodos-letivos/{id}")
    public Mono<ResponseEntity<String>> buscarPeriodoLetivo(
            @PathVariable UUID id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return executar(CatalogReadRoute.PERIODO_LETIVO_POR_ID, authorization, correlationId, id.toString());
    }

    @GetMapping("/api/series")
    public Mono<ResponseEntity<String>> listarSeries(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return executar(CatalogReadRoute.SERIES, authorization, correlationId);
    }

    @GetMapping("/api/series/{id}")
    public Mono<ResponseEntity<String>> buscarSerie(
            @PathVariable UUID id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return executar(CatalogReadRoute.SERIE_POR_ID, authorization, correlationId, id.toString());
    }

    @GetMapping("/api/turnos")
    public Mono<ResponseEntity<String>> listarTurnos(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return executar(CatalogReadRoute.TURNOS, authorization, correlationId);
    }

    @GetMapping("/api/turnos/{id}")
    public Mono<ResponseEntity<String>> buscarTurno(
            @PathVariable UUID id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return executar(CatalogReadRoute.TURNO_POR_ID, authorization, correlationId, id.toString());
    }

    @GetMapping("/api/turmas")
    public Mono<ResponseEntity<String>> listarTurmas(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return executar(CatalogReadRoute.TURMAS, authorization, correlationId);
    }

    @GetMapping("/api/turmas/{id}")
    public Mono<ResponseEntity<String>> buscarTurma(
            @PathVariable UUID id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return executar(CatalogReadRoute.TURMA_POR_ID, authorization, correlationId, id.toString());
    }

    @GetMapping("/api/turmas/{turmaId}/disciplinas")
    public Mono<ResponseEntity<String>> listarDisciplinasDaTurma(
            @PathVariable UUID turmaId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return executar(CatalogReadRoute.TURMA_DISCIPLINAS, authorization, correlationId, turmaId.toString());
    }

    @GetMapping("/api/academico/catalogos/niveis-ensino")
    public Mono<ResponseEntity<String>> listarNiveisEnsino(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return executar(CatalogReadRoute.CATALOGOS_NIVEIS_ENSINO, authorization, correlationId);
    }

    @GetMapping("/api/academico/catalogos/turnos")
    public Mono<ResponseEntity<String>> listarCatalogoTurnos(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return executar(CatalogReadRoute.CATALOGOS_TURNOS, authorization, correlationId);
    }

    private Mono<ResponseEntity<String>> executar(
            CatalogReadRoute route,
            String authorization,
            String correlationId,
            String... pathArgs) {
        return routeCatalogReadUseCase.executar(route, new CatalogReadQuery(authorization, correlationId), pathArgs);
    }
}
