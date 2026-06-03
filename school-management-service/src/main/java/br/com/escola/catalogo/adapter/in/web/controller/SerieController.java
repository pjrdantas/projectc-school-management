package br.com.escola.catalogo.adapter.in.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.catalogo.adapter.in.web.dto.SerieRequest;
import br.com.escola.catalogo.adapter.in.web.dto.SerieResponse;
import br.com.escola.catalogo.application.dto.SerieInput;
import br.com.escola.catalogo.application.dto.SerieOutput;
import br.com.escola.catalogo.application.usecase.AtualizarSerieUseCase;
import br.com.escola.catalogo.application.usecase.BuscarSeriePorIdUseCase;
import br.com.escola.catalogo.application.usecase.CriarSerieUseCase;
import br.com.escola.catalogo.application.usecase.ListarSeriesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/series")
public class SerieController {

    private final CriarSerieUseCase criarSerieUseCase;
    private final AtualizarSerieUseCase atualizarSerieUseCase;
    private final BuscarSeriePorIdUseCase buscarSeriePorIdUseCase;
    private final ListarSeriesUseCase listarSeriesUseCase;

    public SerieController(
            CriarSerieUseCase criarSerieUseCase,
            AtualizarSerieUseCase atualizarSerieUseCase,
            BuscarSeriePorIdUseCase buscarSeriePorIdUseCase,
            ListarSeriesUseCase listarSeriesUseCase) {
        this.criarSerieUseCase = criarSerieUseCase;
        this.atualizarSerieUseCase = atualizarSerieUseCase;
        this.buscarSeriePorIdUseCase = buscarSeriePorIdUseCase;
        this.listarSeriesUseCase = listarSeriesUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma série")
    public SerieResponse criar(@Valid @RequestBody SerieRequest request) {
        return toResponse(criarSerieUseCase.executar(toInput(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma série")
    public SerieResponse atualizar(@PathVariable @NonNull UUID id, @Valid @RequestBody SerieRequest request) {
        return toResponse(atualizarSerieUseCase.executar(id, toInput(request)));
    }

    @GetMapping
    @Operation(summary = "Lista séries")
    public List<SerieResponse> listar() {
        return listarSeriesUseCase.executar().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca série por ID")
    public SerieResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return toResponse(buscarSeriePorIdUseCase.executar(id));
    }

    private SerieInput toInput(SerieRequest request) {
        return new SerieInput(request.nome(), request.ordem(), request.nivelEnsino());
    }

    private SerieResponse toResponse(SerieOutput output) {
        return new SerieResponse(
                output.id(),
                output.nome(),
                output.ordem(),
                output.nivelEnsino(),
                output.createdAt());
    }
}
