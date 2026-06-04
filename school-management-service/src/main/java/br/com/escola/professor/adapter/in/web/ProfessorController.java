package br.com.escola.professor.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.professor.adapter.in.web.dto.ProfessorAlocacaoRequest;
import br.com.escola.professor.adapter.in.web.dto.ProfessorAlocacaoResponse;
import br.com.escola.professor.adapter.in.web.dto.ProfessorRequest;
import br.com.escola.professor.adapter.in.web.dto.ProfessorResponse;
import br.com.escola.professor.application.service.ProfessorService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/professores")
public class ProfessorController {

    private final ProfessorService professorService;

    public ProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um professor a partir de um funcionário")
    public ProfessorResponse criar(@Valid @RequestBody ProfessorRequest request) {
        return professorService.criar(request);
    }

    @GetMapping
    @Operation(summary = "Lista professores")
    public List<ProfessorResponse> listar() {
        return professorService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca professor por ID")
    public ProfessorResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return professorService.buscarPorId(id);
    }

    @PostMapping("/{id}/turmas-disciplinas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Vincula professor a uma turma e disciplina")
    public ProfessorAlocacaoResponse vincularTurmaDisciplina(
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody ProfessorAlocacaoRequest request) {
        return professorService.vincularTurmaDisciplina(id, request);
    }

    @GetMapping("/{id}/turmas-disciplinas")
    @Operation(summary = "Lista vínculos de turma e disciplina do professor")
    public List<ProfessorAlocacaoResponse> listarAlocacoes(@PathVariable @NonNull UUID id) {
        return professorService.listarAlocacoes(id);
    }
}
