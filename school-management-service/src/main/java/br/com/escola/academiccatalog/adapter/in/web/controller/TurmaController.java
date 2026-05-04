package br.com.escola.academiccatalog.adapter.in.web.controller;

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

import br.com.escola.academiccatalog.adapter.in.web.dto.TurmaRequest;
import br.com.escola.academiccatalog.adapter.in.web.dto.TurmaResponse;
import br.com.escola.academiccatalog.application.dto.TurmaInput;
import br.com.escola.academiccatalog.application.dto.TurmaOutput;
import br.com.escola.academiccatalog.application.usecase.BuscarTurmaPorIdUseCase;
import br.com.escola.academiccatalog.application.usecase.CriarTurmaUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/turmas")
public class TurmaController {

    private final CriarTurmaUseCase criarTurmaUseCase;
    private final BuscarTurmaPorIdUseCase buscarTurmaPorIdUseCase;

    public TurmaController(CriarTurmaUseCase criarTurmaUseCase, BuscarTurmaPorIdUseCase buscarTurmaPorIdUseCase) {
        this.criarTurmaUseCase = criarTurmaUseCase;
        this.buscarTurmaPorIdUseCase = buscarTurmaPorIdUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TurmaResponse criar(@Valid @RequestBody TurmaRequest request) {
        TurmaOutput output = criarTurmaUseCase.executar(
                new TurmaInput(request.codigo(), request.nome(), request.capacidade(), request.periodoLetivoId()));
        return toResponse(output);
    }

    @GetMapping("/{id}")
    public TurmaResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return toResponse(buscarTurmaPorIdUseCase.executar(id));
    }

    private TurmaResponse toResponse(TurmaOutput output) {
        return new TurmaResponse(
                output.id(),
                output.codigo(),
                output.nome(),
                output.capacidade(),
                output.periodoLetivoId(),
                output.createdAt());
    }
}
