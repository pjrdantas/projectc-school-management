package br.com.escola.academiccatalog.adapter.in.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.academiccatalog.adapter.in.web.dto.PeriodoLetivoRequest;
import br.com.escola.academiccatalog.adapter.in.web.dto.PeriodoLetivoResponse;
import br.com.escola.academiccatalog.application.dto.PeriodoLetivoInput;
import br.com.escola.academiccatalog.application.dto.PeriodoLetivoOutput;
import br.com.escola.academiccatalog.application.usecase.BuscarPeriodoLetivoPorIdUseCase;
import br.com.escola.academiccatalog.application.usecase.CriarPeriodoLetivoUseCase;
import br.com.escola.academiccatalog.application.usecase.ListarPeriodosLetivosUseCase;
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
    public PeriodoLetivoResponse criar(@Valid @RequestBody PeriodoLetivoRequest request) {
        PeriodoLetivoOutput output = criarPeriodoLetivoUseCase.executar(
                new PeriodoLetivoInput(request.nome(), request.dataInicio(), request.dataFim()));
        return toResponse(output);
    }

    @GetMapping
    public List<PeriodoLetivoResponse> listar() {
        List<PeriodoLetivoResponse> response = listarPeriodosLetivosUseCase.executar().stream().map(this::toResponse).toList();
        if (response.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum período letivo encontrado");
        }
        return response;
    }

    @GetMapping("/{id}")
    public PeriodoLetivoResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return toResponse(buscarPeriodoLetivoPorIdUseCase.executar(id));
    }

    private PeriodoLetivoResponse toResponse(PeriodoLetivoOutput output) {
        return new PeriodoLetivoResponse(
                output.id(),
                output.nome(),
                output.dataInicio(),
                output.dataFim(),
                output.createdAt());
    }
}
