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

import br.com.escola.catalogo.adapter.in.web.dto.TurnoRequest;
import br.com.escola.catalogo.adapter.in.web.dto.TurnoResponse;
import br.com.escola.catalogo.application.dto.TurnoInput;
import br.com.escola.catalogo.application.dto.TurnoOutput;
import br.com.escola.catalogo.application.usecase.AtualizarTurnoUseCase;
import br.com.escola.catalogo.application.usecase.BuscarTurnoPorIdUseCase;
import br.com.escola.catalogo.application.usecase.CriarTurnoUseCase;
import br.com.escola.catalogo.application.usecase.ListarTurnosUseCase;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    private final CriarTurnoUseCase criarTurnoUseCase;
    private final AtualizarTurnoUseCase atualizarTurnoUseCase;
    private final BuscarTurnoPorIdUseCase buscarTurnoPorIdUseCase;
    private final ListarTurnosUseCase listarTurnosUseCase;

    public TurnoController(
            CriarTurnoUseCase criarTurnoUseCase,
            AtualizarTurnoUseCase atualizarTurnoUseCase,
            BuscarTurnoPorIdUseCase buscarTurnoPorIdUseCase,
            ListarTurnosUseCase listarTurnosUseCase) {
        this.criarTurnoUseCase = criarTurnoUseCase;
        this.atualizarTurnoUseCase = atualizarTurnoUseCase;
        this.buscarTurnoPorIdUseCase = buscarTurnoPorIdUseCase;
        this.listarTurnosUseCase = listarTurnosUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um turno")
    public TurnoResponse criar(@Valid @RequestBody TurnoRequest request) {
        return toResponse(criarTurnoUseCase.executar(toInput(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um turno")
    public TurnoResponse atualizar(@PathVariable @NonNull UUID id, @Valid @RequestBody TurnoRequest request) {
        return toResponse(atualizarTurnoUseCase.executar(id, toInput(request)));
    }

    @GetMapping
    @Operation(summary = "Lista turnos")
    public List<TurnoResponse> listar() {
        return listarTurnosUseCase.executar().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca turno por ID")
    public TurnoResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return toResponse(buscarTurnoPorIdUseCase.executar(id));
    }

    private TurnoInput toInput(TurnoRequest request) {
        return new TurnoInput(request.codigo(), request.descricao());
    }

    private TurnoResponse toResponse(TurnoOutput output) {
        return new TurnoResponse(output.id(), output.codigo(), output.descricao());
    }
}
