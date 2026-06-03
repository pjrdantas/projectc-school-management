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

import br.com.escola.catalogo.adapter.in.web.dto.PeriodoLetivoRequest;
import br.com.escola.catalogo.adapter.in.web.dto.PeriodoLetivoResponse;
import br.com.escola.catalogo.application.dto.PeriodoLetivoInput;
import br.com.escola.catalogo.application.dto.PeriodoLetivoOutput;
import br.com.escola.catalogo.application.usecase.BuscarPeriodoLetivoPorIdUseCase;
import br.com.escola.catalogo.application.usecase.CriarPeriodoLetivoUseCase;
import br.com.escola.catalogo.application.usecase.ListarPeriodosLetivosUseCase;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/periodos-letivos")
public class PeriodoLetivoController {

    private final CriarPeriodoLetivoUseCase criarPeriodoLetivoUseCase;
    private final BuscarPeriodoLetivoPorIdUseCase buscarPeriodoLetivoPorIdUseCase;
    private final ListarPeriodosLetivosUseCase listarPeriodosLetivosUseCase;

    public PeriodoLetivoController(
            CriarPeriodoLetivoUseCase criarPeriodoLetivoUseCase,
            BuscarPeriodoLetivoPorIdUseCase buscarPeriodoLetivoPorIdUseCase,
            ListarPeriodosLetivosUseCase listarPeriodosLetivosUseCase) {
        this.criarPeriodoLetivoUseCase = criarPeriodoLetivoUseCase;
        this.buscarPeriodoLetivoPorIdUseCase = buscarPeriodoLetivoPorIdUseCase;
        this.listarPeriodosLetivosUseCase = listarPeriodosLetivosUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um período letivo")
    public PeriodoLetivoResponse criar(@Valid @RequestBody PeriodoLetivoRequest request) {
        PeriodoLetivoOutput output = criarPeriodoLetivoUseCase.executar(
                new PeriodoLetivoInput(request.nome(), request.ano(), request.dataInicio(), request.dataFim()));
        return toResponse(output);
    }

    @GetMapping
    @Operation(summary = "Lista períodos letivos")
    public List<PeriodoLetivoResponse> listar() {
        return listarPeriodosLetivosUseCase.executar().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca período letivo por ID")
    public PeriodoLetivoResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return toResponse(buscarPeriodoLetivoPorIdUseCase.executar(id));
    }

    private PeriodoLetivoResponse toResponse(PeriodoLetivoOutput output) {
        return new PeriodoLetivoResponse(
                output.id(),
                output.nome(),
                output.ano(),
                output.dataInicio(),
                output.dataFim(),
                output.ativo(),
                output.createdAt());
    }
}
