package br.com.escola.enrollment.adapter.in.web;

import java.util.UUID;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.enrollment.application.dto.MatriculaFiltro;
import br.com.escola.enrollment.application.dto.MatriculaInput;
import br.com.escola.enrollment.application.dto.MatriculaOutput;
import br.com.escola.enrollment.application.usecase.ConsultarMatriculasUseCase;
import br.com.escola.enrollment.application.usecase.CriarMatriculaUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/matriculas")
public class MatriculaController {

    private final CriarMatriculaUseCase criarMatriculaUseCase;
    private final ConsultarMatriculasUseCase consultarMatriculasUseCase;

    public MatriculaController(
            CriarMatriculaUseCase criarMatriculaUseCase,
            ConsultarMatriculasUseCase consultarMatriculasUseCase) {
        this.criarMatriculaUseCase = criarMatriculaUseCase;
        this.consultarMatriculasUseCase = consultarMatriculasUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatriculaResponse criar(@Valid @RequestBody MatriculaRequest request) {
        MatriculaOutput output = criarMatriculaUseCase.executar(
                new MatriculaInput(request.alunoId(), request.turmaId(), request.periodoLetivoId()));
        return toResponse(output);
    }

    @GetMapping
    public List<MatriculaResponse> consultar(
            @RequestParam(required = false) UUID alunoId,
            @RequestParam(required = false) UUID turmaId,
            @RequestParam(required = false) UUID periodoLetivoId,
            @RequestParam(required = false) String status) {
        MatriculaFiltro filtro = new MatriculaFiltro(alunoId, turmaId, periodoLetivoId, status);
        List<MatriculaResponse> response = consultarMatriculasUseCase.executar(filtro).stream().map(this::toResponse).toList();
        if (response.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhuma matrícula encontrada");
        }
        return response;
    }

    private MatriculaResponse toResponse(MatriculaOutput output) {
        return new MatriculaResponse(
                output.id(),
                output.alunoId(),
                output.turmaId(),
                output.periodoLetivoId(),
                output.status(),
                output.createdAt());
    }
}
