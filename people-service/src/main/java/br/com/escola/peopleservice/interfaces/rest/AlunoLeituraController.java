package br.com.escola.peopleservice.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.peopleservice.application.context.InternalHeaders;
import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.AlunoFichaResponse;
import br.com.escola.peopleservice.application.dto.AlunoResponse;
import br.com.escola.peopleservice.application.port.in.ConsultarAlunoUseCase;

@RestController
@RequestMapping("/internal/v1/alunos")
public class AlunoLeituraController {

    private final ConsultarAlunoUseCase consultarAlunoUseCase;

    public AlunoLeituraController(ConsultarAlunoUseCase consultarAlunoUseCase) {
        this.consultarAlunoUseCase = consultarAlunoUseCase;
    }

    @GetMapping
    public List<AlunoResponse> listar(
            @RequestParam(required = false) String nome,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return consultarAlunoUseCase.listar(nome, context);
    }

    @GetMapping("/{alunoId}")
    public AlunoResponse buscar(
            @PathVariable UUID alunoId,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return consultarAlunoUseCase.buscar(alunoId, context);
    }

    @GetMapping("/{alunoId}/ficha")
    public AlunoFichaResponse buscarFicha(
            @PathVariable UUID alunoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return consultarAlunoUseCase.buscarFicha(alunoId, authorization, context);
    }
}
