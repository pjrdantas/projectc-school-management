package br.com.escola.catalogo.adapter.in.web.controller;

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

import br.com.escola.catalogo.adapter.in.web.dto.TurmaDisciplinaRequest;
import br.com.escola.catalogo.adapter.in.web.dto.TurmaDisciplinaResponse;
import br.com.escola.catalogo.application.service.TurmaDisciplinaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/turmas/{turmaId}/disciplinas")
public class TurmaDisciplinaController {

    private final TurmaDisciplinaService turmaDisciplinaService;

    public TurmaDisciplinaController(TurmaDisciplinaService turmaDisciplinaService) {
        this.turmaDisciplinaService = turmaDisciplinaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Vincula uma disciplina a uma turma")
    public TurmaDisciplinaResponse vincular(
            @PathVariable @NonNull UUID turmaId,
            @Valid @RequestBody TurmaDisciplinaRequest request) {
        return turmaDisciplinaService.vincular(turmaId, request);
    }

    @GetMapping
    @Operation(summary = "Lista disciplinas vinculadas a uma turma")
    public List<TurmaDisciplinaResponse> listarPorTurma(@PathVariable @NonNull UUID turmaId) {
        return turmaDisciplinaService.listarPorTurma(turmaId);
    }
}
