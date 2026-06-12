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

import br.com.escola.catalogo.adapter.in.web.dto.TurmaRequest;
import br.com.escola.catalogo.adapter.in.web.dto.TurmaResponse;
import br.com.escola.catalogo.application.dto.TurmaInput;
import br.com.escola.catalogo.application.dto.TurmaOutput;
import br.com.escola.catalogo.application.usecase.AtualizarTurmaUseCase;
import br.com.escola.catalogo.application.usecase.BuscarTurmaPorIdUseCase;
import br.com.escola.catalogo.application.usecase.CriarTurmaUseCase;
import br.com.escola.catalogo.application.usecase.ListarTurmasUseCase;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/turmas")
public class TurmaController {

    private final CriarTurmaUseCase criarTurmaUseCase;
    private final AtualizarTurmaUseCase atualizarTurmaUseCase;
    private final BuscarTurmaPorIdUseCase buscarTurmaPorIdUseCase;
    private final ListarTurmasUseCase listarTurmasUseCase;

    public TurmaController(
            CriarTurmaUseCase criarTurmaUseCase,
            AtualizarTurmaUseCase atualizarTurmaUseCase,
            BuscarTurmaPorIdUseCase buscarTurmaPorIdUseCase,
            ListarTurmasUseCase listarTurmasUseCase) {
        this.criarTurmaUseCase = criarTurmaUseCase;
        this.atualizarTurmaUseCase = atualizarTurmaUseCase;
        this.buscarTurmaPorIdUseCase = buscarTurmaPorIdUseCase;
        this.listarTurmasUseCase = listarTurmasUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma turma")
    public TurmaResponse criar(@Valid @RequestBody TurmaRequest request) {
        TurmaOutput output = criarTurmaUseCase.executar(toInput(request));
        return toResponse(output);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma turma")
    public TurmaResponse atualizar(@PathVariable @NonNull UUID id, @Valid @RequestBody TurmaRequest request) {
        TurmaOutput output = atualizarTurmaUseCase.executar(
                id,
                toInput(request));
        return toResponse(output);
    }

    @GetMapping
    @Operation(summary = "Lista turmas")
    public List<TurmaResponse> listar() {
        return listarTurmasUseCase.executar().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca turma por ID")
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
                output.serieId(),
                output.serieNome(),
                output.turno(),
                output.status(),
                output.escolaId(),
                output.escolaNome(),
                output.createdAt());
    }

    private TurmaInput toInput(TurmaRequest request) {
        return new TurmaInput(
                request.codigo(),
                request.nome(),
                request.capacidade(),
                request.periodoLetivoId(),
                request.serieId(),
                request.turno(),
                request.status(),
                request.escolaId());
    }
}
