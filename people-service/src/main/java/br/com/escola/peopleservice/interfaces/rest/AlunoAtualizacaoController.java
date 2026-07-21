package br.com.escola.peopleservice.interfaces.rest;

import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.peopleservice.application.context.InternalHeaders;
import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.AlunoAtualizacaoRequest;
import br.com.escola.peopleservice.application.dto.AlunoResponse;
import br.com.escola.peopleservice.application.port.in.AtualizarAlunoUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal/v1/alunos")
public class AlunoAtualizacaoController {

    private final AtualizarAlunoUseCase atualizarAlunoUseCase;

    public AlunoAtualizacaoController(AtualizarAlunoUseCase atualizarAlunoUseCase) {
        this.atualizarAlunoUseCase = atualizarAlunoUseCase;
    }

    @PutMapping("/{alunoId}")
    public AlunoResponse atualizar(
            @PathVariable UUID alunoId,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @Valid @RequestBody AlunoAtualizacaoRequest request) {
        return AlunoResponse.from(atualizarAlunoUseCase.atualizar(alunoId, request, context));
    }
}
