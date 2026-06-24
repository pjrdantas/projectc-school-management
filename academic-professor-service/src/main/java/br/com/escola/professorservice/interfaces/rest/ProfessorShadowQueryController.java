package br.com.escola.professorservice.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.professorservice.application.context.InternalHeaders;
import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.FuncionarioElegivelResponse;
import br.com.escola.professorservice.application.dto.ProfessorAlocacaoResponse;
import br.com.escola.professorservice.application.dto.ProfessorResumoResponse;
import br.com.escola.professorservice.application.port.in.ProfessorQueryUseCase;

@RestController
@RequestMapping("/internal/v1")
public class ProfessorShadowQueryController {

    private final ProfessorQueryUseCase professorQueryUseCase;

    public ProfessorShadowQueryController(ProfessorQueryUseCase professorQueryUseCase) {
        this.professorQueryUseCase = professorQueryUseCase;
    }

    @GetMapping("/professores")
    public List<ProfessorResumoResponse> listarProfessores(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return professorQueryUseCase.listarProfessores(authorization, context);
    }

    @GetMapping("/professores/{id}")
    public ProfessorResumoResponse buscarProfessorPorId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return professorQueryUseCase.buscarProfessorPorId(authorization, context, id);
    }

    @GetMapping("/professores/{id}/turmas-disciplinas")
    public List<ProfessorAlocacaoResponse> listarAlocacoes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return professorQueryUseCase.listarAlocacoes(authorization, context, id);
    }

    @GetMapping("/turmas/{turmaId}/professores")
    public List<ProfessorAlocacaoResponse> listarProfessoresPorTurma(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID turmaId) {
        return professorQueryUseCase.listarProfessoresPorTurma(authorization, context, turmaId);
    }

    @GetMapping("/professores/funcionarios-elegiveis")
    public List<FuncionarioElegivelResponse> listarFuncionariosElegiveis(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return professorQueryUseCase.listarFuncionariosElegiveis(authorization, context);
    }
}
