package br.com.escola.peopleservice.interfaces.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.peopleservice.application.context.InternalHeaders;
import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.AlunoCriacaoRequest;
import br.com.escola.peopleservice.application.dto.AlunoResponse;
import br.com.escola.peopleservice.application.port.in.CriarAlunoUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal/v1/alunos")
public class AlunoCriacaoController {

    private final CriarAlunoUseCase criarAlunoUseCase;

    public AlunoCriacaoController(CriarAlunoUseCase criarAlunoUseCase) {
        this.criarAlunoUseCase = criarAlunoUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlunoResponse criar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @Valid @RequestBody AlunoCriacaoRequest request) {
        return AlunoResponse.from(criarAlunoUseCase.criar(request, authorization, context));
    }
}
