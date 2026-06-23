package br.com.escola.professor.adapter.in.web.internal;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.professor.adapter.in.web.dto.internal.ProfessorAlocacaoInternalRequest;
import br.com.escola.professor.adapter.in.web.dto.internal.ProfessorAlocacaoInternalResponse;
import br.com.escola.professor.adapter.in.web.dto.internal.ProfessorInternalRequest;
import br.com.escola.professor.adapter.in.web.dto.internal.ProfessorInternalResponse;
import br.com.escola.professor.application.dto.internal.AlocarProfessorTurmaDisciplinaSolicitacao;
import br.com.escola.professor.application.dto.internal.CriarProfessorSolicitacao;
import br.com.escola.professor.application.dto.internal.ProfessorAlocacaoResumo;
import br.com.escola.professor.application.dto.internal.ProfessorResumo;
import br.com.escola.professor.application.port.internal.ProfessorAcademicoPort;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal/professores")
public class ProfessorInternalController {

    private static final String ESCOLA_HEADER = "X-Escola-Id";

    private final ProfessorAcademicoPort professorAcademicoPort;

    public ProfessorInternalController(
            @Qualifier("professorService") ProfessorAcademicoPort professorAcademicoPort) {
        this.professorAcademicoPort = professorAcademicoPort;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria professor para uso interno entre backends")
    public ProfessorInternalResponse criar(
            @RequestHeader(ESCOLA_HEADER) UUID escolaId,
            @Valid @RequestBody ProfessorInternalRequest request) {
        return toResponse(professorAcademicoPort.criarProfessor(
                escolaId,
                new CriarProfessorSolicitacao(
                        request.funcionarioId(),
                        request.registroProfissional(),
                        request.formacao(),
                        request.ativo())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca professor por id para uso interno entre backends")
    public ProfessorInternalResponse buscarPorId(
            @RequestHeader(ESCOLA_HEADER) UUID escolaId,
            @PathVariable @NonNull UUID id) {
        return professorAcademicoPort.buscarProfessor(escolaId, id)
                .map(this::toResponse)
                .orElseThrow(br.com.escola.professor.domain.exception.ProfessorNaoEncontradoException::new);
    }

    @PostMapping("/{id}/turmas-disciplinas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Aloca professor em turma-disciplina para uso interno entre backends")
    public ProfessorAlocacaoInternalResponse alocar(
            @RequestHeader(ESCOLA_HEADER) UUID escolaId,
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody ProfessorAlocacaoInternalRequest request) {
        return toResponse(professorAcademicoPort.alocarProfessorTurmaDisciplina(
                escolaId,
                id,
                new AlocarProfessorTurmaDisciplinaSolicitacao(
                        request.turmaDisciplinaId(),
                        request.dataInicio(),
                        request.dataFim(),
                        request.ativo())));
    }

    @GetMapping("/{id}/turmas-disciplinas")
    @Operation(summary = "Lista alocações do professor para uso interno entre backends")
    public List<ProfessorAlocacaoInternalResponse> listarAlocacoes(
            @RequestHeader(ESCOLA_HEADER) UUID escolaId,
            @PathVariable @NonNull UUID id) {
        return professorAcademicoPort.listarAlocacoes(escolaId, id).stream()
                .map(this::toResponse)
                .toList();
    }

    private ProfessorInternalResponse toResponse(ProfessorResumo resumo) {
        return new ProfessorInternalResponse(
                resumo.id(),
                resumo.pessoaId(),
                resumo.nomeCompleto(),
                resumo.escolaId(),
                resumo.escolaNome(),
                resumo.registroProfissional(),
                resumo.formacao(),
                resumo.ativo(),
                resumo.createdAt(),
                resumo.updatedAt());
    }

    private ProfessorAlocacaoInternalResponse toResponse(ProfessorAlocacaoResumo resumo) {
        return new ProfessorAlocacaoInternalResponse(
                resumo.id(),
                resumo.professorId(),
                resumo.professorNome(),
                resumo.turmaDisciplinaId(),
                resumo.turmaId(),
                resumo.turmaNome(),
                resumo.disciplinaId(),
                resumo.disciplinaNome(),
                resumo.dataInicio(),
                resumo.dataFim(),
                resumo.ativo(),
                resumo.createdAt());
    }
}
