package br.com.escola.professorservice.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.professorservice.application.context.InternalHeaders;
import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.FuncionarioElegivelResponse;
import br.com.escola.professorservice.application.dto.AllocateRequest;
import br.com.escola.professorservice.application.dto.CreateRequest;
import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.application.dto.ResumoResponse;
import br.com.escola.professorservice.application.dto.UpdateRequest;
import br.com.escola.professorservice.application.dto.UpdateAllocateRequest;
import br.com.escola.professorservice.application.port.in.ComandoUseCase;
import br.com.escola.professorservice.application.port.in.ConsultaUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class ConsultaInternaController {

    private final ConsultaUseCase professorQueryUseCase;
    private final ComandoUseCase professorCommandUseCase;

    public ConsultaInternaController(
            ConsultaUseCase professorQueryUseCase,
            ComandoUseCase professorCommandUseCase) {
        this.professorQueryUseCase = professorQueryUseCase;
        this.professorCommandUseCase = professorCommandUseCase;
    }

    @PostMapping("/professores")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumoResponse criarProfessor(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @Valid @RequestBody CreateRequest request) {
        return professorCommandUseCase.criarProfessor(authorization, context, request);
    }

    @PutMapping("/professores/{id}")
    public ResumoResponse atualizarProfessor(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody UpdateRequest request) {
        return professorCommandUseCase.atualizarProfessor(context, id, request);
    }

    @PostMapping("/professores/{id}/turmas-disciplinas")
    @ResponseStatus(HttpStatus.CREATED)
    public AlocacaoResponse alocarProfessorTurmaDisciplina(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody AllocateRequest request) {
        return professorCommandUseCase.alocarProfessorTurmaDisciplina(authorization, context, id, request);
    }

    @PutMapping("/professores/{professorId}/turmas-disciplinas/{alocacaoId}")
    public AlocacaoResponse atualizarAlocacaoProfessorTurmaDisciplina(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID professorId,
            @PathVariable @NonNull UUID alocacaoId,
            @Valid @RequestBody UpdateAllocateRequest request) {
        return professorCommandUseCase.atualizarAlocacaoProfessorTurmaDisciplina(
                authorization, context, professorId, alocacaoId, request);
    }

    @DeleteMapping("/professores/{professorId}/turmas-disciplinas/{alocacaoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void encerrarAlocacaoProfessorTurmaDisciplina(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID professorId,
            @PathVariable @NonNull UUID alocacaoId) {
        professorCommandUseCase.encerrarAlocacaoProfessorTurmaDisciplina(context, professorId, alocacaoId);
    }

    @GetMapping("/professores")
    public List<ResumoResponse> listarProfessores(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return professorQueryUseCase.listarProfessores(authorization, context);
    }

    @GetMapping("/professores/{id}")
    public ResumoResponse buscarProfessorPorId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return professorQueryUseCase.buscarProfessorPorId(authorization, context, id);
    }

    @GetMapping("/professores/{id}/turmas-disciplinas")
    public List<AlocacaoResponse> listarAlocacoes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return professorQueryUseCase.listarAlocacoes(authorization, context, id);
    }

    @GetMapping({ "/turmas/{turmaId}/professores", "/professores/turmas/{turmaId}" })
    public List<AlocacaoResponse> listarProfessoresPorTurma(
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

