package br.com.escola.responsiblesservice.interfaces.rest;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.responsiblesservice.application.context.InternalHeaders;
import br.com.escola.responsiblesservice.application.context.InternalRequestContext;
import br.com.escola.responsiblesservice.application.port.in.ResponsavelQueryUseCase;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class ResponsavelInternalController {

    private final ResponsavelQueryUseCase responsavelQueryUseCase;

    public ResponsavelInternalController(ResponsavelQueryUseCase responsavelQueryUseCase) {
        this.responsavelQueryUseCase = responsavelQueryUseCase;
    }

    @GetMapping("/responsaveis/{id}")
    public ResponseEntity<String> buscarResponsavelPorId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return responsavelQueryUseCase.buscarResponsavelPorId(authorization, context, id);
    }

    @GetMapping("/alunos/{alunoId}/responsaveis")
    public ResponseEntity<String> listarResponsaveisPorAluno(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID alunoId) {
        return responsavelQueryUseCase.listarResponsaveisPorAluno(authorization, context, alunoId);
    }
}
