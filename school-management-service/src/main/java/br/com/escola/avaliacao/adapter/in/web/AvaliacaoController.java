package br.com.escola.avaliacao.adapter.in.web;

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

import br.com.escola.avaliacao.adapter.in.web.dto.AvaliacaoRequest;
import br.com.escola.avaliacao.adapter.in.web.dto.AvaliacaoResponse;
import br.com.escola.avaliacao.adapter.in.web.dto.NotaAlunoRequest;
import br.com.escola.avaliacao.adapter.in.web.dto.NotaAlunoResponse;
import br.com.escola.avaliacao.application.service.AvaliacaoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma avaliação")
    public AvaliacaoResponse criar(@Valid @RequestBody AvaliacaoRequest request) {
        return avaliacaoService.criar(request);
    }

    @GetMapping
    @Operation(summary = "Lista avaliações")
    public List<AvaliacaoResponse> listar(
            @RequestParam(required = false) UUID professorTurmaDisciplinaId,
            @RequestParam(required = false) UUID turmaId) {
        return avaliacaoService.listar(professorTurmaDisciplinaId, turmaId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca avaliação por ID")
    public AvaliacaoResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return avaliacaoService.buscarPorId(id);
    }

    @PostMapping("/{id}/notas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Lança nota de aluno na avaliação")
    public NotaAlunoResponse lancarNota(
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody NotaAlunoRequest request) {
        return avaliacaoService.lancarNota(id, request);
    }

    @GetMapping("/{id}/notas")
    @Operation(summary = "Lista notas da avaliação")
    public List<NotaAlunoResponse> listarNotas(@PathVariable @NonNull UUID id) {
        return avaliacaoService.listarNotasPorAvaliacao(id);
    }
}
