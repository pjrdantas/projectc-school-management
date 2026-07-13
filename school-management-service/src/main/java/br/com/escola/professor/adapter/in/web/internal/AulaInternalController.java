package br.com.escola.professor.adapter.in.web.internal;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.professor.adapter.in.web.dto.AulaRequest;
import br.com.escola.professor.adapter.in.web.dto.AulaResponse;
import br.com.escola.professor.adapter.in.web.dto.FrequenciaAlunoRequest;
import br.com.escola.professor.adapter.in.web.dto.FrequenciaAlunoResponse;
import br.com.escola.professor.adapter.in.web.dto.FrequenciaProfessorRequest;
import br.com.escola.professor.adapter.in.web.dto.FrequenciaProfessorResponse;
import br.com.escola.professor.application.service.DiarioAulaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal/aulas")
public class AulaInternalController {

    private final DiarioAulaService diarioAulaService;

    public AulaInternalController(DiarioAulaService diarioAulaService) {
        this.diarioAulaService = diarioAulaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AulaResponse criar(@Valid @RequestBody AulaRequest request) {
        return diarioAulaService.criarAula(request);
    }

    @GetMapping
    public List<AulaResponse> listar(
            @RequestParam(required = false) UUID professorTurmaDisciplinaId,
            @RequestParam(required = false) UUID turmaId) {
        return diarioAulaService.listarAulas(professorTurmaDisciplinaId, turmaId);
    }

    @GetMapping("/{id}")
    public AulaResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return diarioAulaService.buscarAulaPorId(id);
    }

    @PostMapping("/{id}/frequencia-professor")
    @ResponseStatus(HttpStatus.CREATED)
    public FrequenciaProfessorResponse registrarFrequenciaProfessor(
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody FrequenciaProfessorRequest request) {
        return diarioAulaService.registrarFrequenciaProfessor(id, request);
    }

    @GetMapping("/{id}/frequencia-professor")
    public List<FrequenciaProfessorResponse> listarFrequenciaProfessor(@PathVariable @NonNull UUID id) {
        return diarioAulaService.listarFrequenciaProfessor(id);
    }

    @PostMapping("/{id}/frequencias-alunos")
    @ResponseStatus(HttpStatus.CREATED)
    public FrequenciaAlunoResponse registrarFrequenciaAluno(
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody FrequenciaAlunoRequest request) {
        return diarioAulaService.registrarFrequenciaAluno(id, request);
    }

    @GetMapping("/{id}/frequencias-alunos")
    public List<FrequenciaAlunoResponse> listarFrequenciasAlunos(@PathVariable @NonNull UUID id) {
        return diarioAulaService.listarFrequenciasAlunos(id);
    }
}
