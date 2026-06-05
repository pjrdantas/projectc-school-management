package br.com.escola.catalogo.adapter.in.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.catalogo.adapter.in.web.dto.DisciplinaRequest;
import br.com.escola.catalogo.adapter.in.web.dto.DisciplinaResponse;
import br.com.escola.catalogo.application.service.DisciplinaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/disciplinas")
public class DisciplinaController {

    private final DisciplinaService disciplinaService;

    public DisciplinaController(DisciplinaService disciplinaService) {
        this.disciplinaService = disciplinaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma disciplina")
    public DisciplinaResponse criar(@Valid @RequestBody DisciplinaRequest request) {
        return disciplinaService.criar(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma disciplina")
    public DisciplinaResponse atualizar(@PathVariable @NonNull UUID id, @Valid @RequestBody DisciplinaRequest request) {
        return disciplinaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui uma disciplina")
    public void excluir(@PathVariable @NonNull UUID id) {
        disciplinaService.excluir(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca disciplina por ID")
    public DisciplinaResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return disciplinaService.buscarPorId(id);
    }

    @GetMapping
    @Operation(summary = "Lista disciplinas")
    public List<DisciplinaResponse> listar() {
        return disciplinaService.listar();
    }
}
