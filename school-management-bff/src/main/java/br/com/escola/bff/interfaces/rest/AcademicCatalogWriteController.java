package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.DisciplinaCreateCommand;
import br.com.escola.bff.application.dto.PeriodoLetivoCreateCommand;
import br.com.escola.bff.application.usecase.CreateDisciplinaUseCase;
import br.com.escola.bff.application.usecase.CreatePeriodoLetivoUseCase;
import br.com.escola.bff.interfaces.request.DisciplinaRequest;
import br.com.escola.bff.interfaces.request.PeriodoLetivoRequest;
import br.com.escola.bff.interfaces.response.DisciplinaResponse;
import br.com.escola.bff.interfaces.response.PeriodoLetivoResponse;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.catalog-write-proxy-enabled", havingValue = "true")
public class AcademicCatalogWriteController {

    private final CreatePeriodoLetivoUseCase createPeriodoLetivoUseCase;
    private final CreateDisciplinaUseCase createDisciplinaUseCase;

    public AcademicCatalogWriteController(
            CreatePeriodoLetivoUseCase createPeriodoLetivoUseCase,
            CreateDisciplinaUseCase createDisciplinaUseCase) {
        this.createPeriodoLetivoUseCase = createPeriodoLetivoUseCase;
        this.createDisciplinaUseCase = createDisciplinaUseCase;
    }

    @PostMapping("/api/periodos-letivos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<PeriodoLetivoResponse>> criarPeriodoLetivo(
            @Valid @RequestBody PeriodoLetivoRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        String resolvedKey = idempotencyKey != null && !idempotencyKey.isBlank()
                ? idempotencyKey
                : UUID.randomUUID().toString();
        return createPeriodoLetivoUseCase.executar(
                        new CatalogWriteQuery(authorization, correlationId, resolvedKey),
                        new PeriodoLetivoCreateCommand(
                                request.nome(),
                                request.ano(),
                                request.dataInicio(),
                                request.dataFim(),
                                request.escolaId()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(new PeriodoLetivoResponse(
                        body.id(),
                        body.nome(),
                        body.ano(),
                        body.dataInicio(),
                        body.dataFim(),
                        body.ativo(),
                        body.escolaId(),
                        body.escolaNome(),
                        body.createdAt())));
    }

    @PostMapping("/api/disciplinas")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<DisciplinaResponse>> criarDisciplina(
            @Valid @RequestBody DisciplinaRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        String resolvedKey = idempotencyKey != null && !idempotencyKey.isBlank()
                ? idempotencyKey
                : UUID.randomUUID().toString();
        return createDisciplinaUseCase.executar(
                        new CatalogWriteQuery(authorization, correlationId, resolvedKey),
                        new DisciplinaCreateCommand(
                                request.nome(),
                                request.cargaHoraria(),
                                request.status(),
                                request.escolaId()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(new DisciplinaResponse(
                        body.id(),
                        body.nome(),
                        body.cargaHoraria(),
                        body.status(),
                        body.escolaId(),
                        body.escolaNome(),
                        body.createdAt())));
    }
}
