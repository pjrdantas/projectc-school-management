package br.com.escola.matricula.adapter.in.web.internal;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.matricula.application.dto.MatriculaEtapaOutput;
import br.com.escola.matricula.application.dto.MatriculaFiltro;
import br.com.escola.matricula.application.dto.MatriculaOutput;
import br.com.escola.matricula.application.usecase.ConsultarMatriculasUseCase;

@RestController
@RequestMapping("/internal/matriculas")
public class MatriculaInternalController {

    private final ConsultarMatriculasUseCase consultarMatriculasUseCase;

    public MatriculaInternalController(ConsultarMatriculasUseCase consultarMatriculasUseCase) {
        this.consultarMatriculasUseCase = consultarMatriculasUseCase;
    }

    @GetMapping
    public List<MatriculaInternalResponse> consultar(
            @RequestParam(required = false) UUID alunoId,
            @RequestParam(required = false) UUID turmaId,
            @RequestParam(required = false) UUID periodoLetivoId,
            @RequestParam(required = false) String status) {
        MatriculaFiltro filtro = new MatriculaFiltro(alunoId, turmaId, periodoLetivoId, status);
        return consultarMatriculasUseCase.executar(filtro).stream()
                .map(this::toResponse)
                .toList();
    }

    private MatriculaInternalResponse toResponse(MatriculaOutput output) {
        return new MatriculaInternalResponse(
                output.id(),
                output.alunoId(),
                output.turmaId(),
                output.escolaId(),
                output.escolaNome(),
                output.serieId(),
                output.serieNome(),
                output.periodoLetivoId(),
                output.status(),
                output.tipoMatricula(),
                output.dataMatricula(),
                output.observacao(),
                output.createdAt(),
                output.etapas().stream().map(this::toEtapaResponse).toList());
    }

    private MatriculaEtapaInternalResponse toEtapaResponse(MatriculaEtapaOutput output) {
        return new MatriculaEtapaInternalResponse(
                output.id(),
                output.descricao(),
                output.ordem(),
                output.status(),
                output.dataInicio(),
                output.dataConclusao(),
                output.observacao());
    }
}
