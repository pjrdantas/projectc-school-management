package br.com.escola.professor.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.professor.adapter.in.web.dto.ProfessorAlocacaoResponse;
import br.com.escola.professor.application.service.ProfessorService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/turmas/{turmaId}/professores")
public class TurmaProfessorController {

    private final ProfessorService professorService;

    public TurmaProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @GetMapping
    @Operation(summary = "Lista professores vinculados a uma turma")
    public List<ProfessorAlocacaoResponse> listarPorTurma(@PathVariable @NonNull UUID turmaId) {
        return professorService.listarPorTurma(turmaId);
    }
}
