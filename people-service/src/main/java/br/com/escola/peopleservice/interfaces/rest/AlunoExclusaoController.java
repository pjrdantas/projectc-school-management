package br.com.escola.peopleservice.interfaces.rest;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.peopleservice.application.context.InternalHeaders;
import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.port.in.ExcluirAlunoUseCase;

@RestController
@RequestMapping("/internal/v1/alunos")
public class AlunoExclusaoController {

    private final ExcluirAlunoUseCase excluirAlunoUseCase;

    public AlunoExclusaoController(ExcluirAlunoUseCase excluirAlunoUseCase) {
        this.excluirAlunoUseCase = excluirAlunoUseCase;
    }

    @DeleteMapping("/{alunoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(
            @PathVariable UUID alunoId,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        excluirAlunoUseCase.excluir(alunoId, context);
    }
}
