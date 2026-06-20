package br.com.escola.bff.interfaces.rest;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.DisciplinaQuery;
import br.com.escola.bff.application.dto.DisciplinaView;
import br.com.escola.bff.application.usecase.ListarDisciplinasUseCase;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/disciplinas")
@ConditionalOnProperty(name = "features.disciplinas-proxy-enabled", havingValue = "true")
public class DisciplinaController {

    private final ListarDisciplinasUseCase listarDisciplinasUseCase;

    public DisciplinaController(ListarDisciplinasUseCase listarDisciplinasUseCase) {
        this.listarDisciplinasUseCase = listarDisciplinasUseCase;
    }

    @GetMapping
    public Mono<List<DisciplinaView>> listar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return listarDisciplinasUseCase.executar(new DisciplinaQuery(authorization, correlationId));
    }
}
