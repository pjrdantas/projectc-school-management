package br.com.escola.planejamento.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.planejamento.adapter.in.web.dto.PlanejamentoBimestralAulaRequest;
import br.com.escola.planejamento.adapter.in.web.dto.PlanejamentoBimestralAulaResponse;
import br.com.escola.planejamento.adapter.in.web.dto.PlanejamentoBimestralAvaliacaoRequest;
import br.com.escola.planejamento.adapter.in.web.dto.PlanejamentoBimestralAvaliacaoResponse;
import br.com.escola.planejamento.adapter.in.web.dto.PlanejamentoBimestralRequest;
import br.com.escola.planejamento.adapter.in.web.dto.PlanejamentoBimestralResponse;
import br.com.escola.planejamento.adapter.in.web.dto.PlanejamentoBimestralStatusRequest;
import br.com.escola.planejamento.application.service.PlanejamentoBimestralService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/planejamentos-bimestrais")
public class PlanejamentoBimestralController {

    private final PlanejamentoBimestralService planejamentoBimestralService;

    public PlanejamentoBimestralController(PlanejamentoBimestralService planejamentoBimestralService) {
        this.planejamentoBimestralService = planejamentoBimestralService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria planejamento bimestral")
    public PlanejamentoBimestralResponse criar(@Valid @RequestBody PlanejamentoBimestralRequest request) {
        return planejamentoBimestralService.criar(request);
    }

    @GetMapping
    @Operation(summary = "Lista planejamentos bimestrais")
    public List<PlanejamentoBimestralResponse> listar(
            @RequestParam(required = false) UUID professorId,
            @RequestParam(required = false) UUID turmaId,
            @RequestParam(required = false) UUID disciplinaId,
            @RequestParam(required = false) UUID periodoAvaliativoId) {
        return planejamentoBimestralService.listar(professorId, turmaId, disciplinaId, periodoAvaliativoId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca planejamento bimestral por ID")
    public PlanejamentoBimestralResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return planejamentoBimestralService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza planejamento bimestral")
    public PlanejamentoBimestralResponse atualizar(
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody PlanejamentoBimestralRequest request) {
        return planejamentoBimestralService.atualizar(id, request);
    }

    @PostMapping("/{id}/aulas-previstas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adiciona aula prevista ao planejamento bimestral")
    public PlanejamentoBimestralAulaResponse adicionarAulaPrevista(
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody PlanejamentoBimestralAulaRequest request) {
        return planejamentoBimestralService.adicionarAulaPrevista(id, request);
    }

    @PostMapping("/{id}/avaliacoes-previstas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adiciona avaliação prevista ao planejamento bimestral")
    public PlanejamentoBimestralAvaliacaoResponse adicionarAvaliacaoPrevista(
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody PlanejamentoBimestralAvaliacaoRequest request) {
        return planejamentoBimestralService.adicionarAvaliacaoPrevista(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Altera status do planejamento bimestral")
    public PlanejamentoBimestralResponse alterarStatus(
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody PlanejamentoBimestralStatusRequest request) {
        return planejamentoBimestralService.alterarStatus(id, request);
    }
}
