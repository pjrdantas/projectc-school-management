package br.com.escola.historico.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarRequest;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarResponse;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarGeracaoRequest;
import br.com.escola.historico.application.service.HistoricoEscolarService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/historicos-escolares")
@RequiredArgsConstructor
public class HistoricoEscolarController {

    private final HistoricoEscolarService historicoEscolarService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um histórico escolar")
    public HistoricoEscolarResponse criar(@Valid @RequestBody HistoricoEscolarRequest request) {
        return historicoEscolarService.criar(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um histórico escolar")
    public HistoricoEscolarResponse atualizar(
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody HistoricoEscolarRequest request) {
        return historicoEscolarService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui um histórico escolar")
    public void excluir(@PathVariable @NonNull UUID id) {
        historicoEscolarService.excluir(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca histórico escolar por ID")
    public HistoricoEscolarResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return historicoEscolarService.buscarPorId(id);
    }

    @GetMapping("/alunos/{alunoId}")
    @Operation(summary = "Lista históricos escolares por aluno")
    public List<HistoricoEscolarResponse> listarPorAluno(@PathVariable @NonNull UUID alunoId) {
        return historicoEscolarService.listarPorAluno(alunoId);
    }

    @GetMapping
    @Operation(summary = "Lista históricos escolares")
    public Page<HistoricoEscolarResponse> listar(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return historicoEscolarService.listar(pageable);
    }

    @PostMapping("/matriculas/{matriculaId}/geracao-por-boletim")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Gera histórico escolar a partir de boletim fechado")
    public HistoricoEscolarResponse gerarPorBoletim(
            @PathVariable @NonNull UUID matriculaId,
            @Valid @RequestBody HistoricoEscolarGeracaoRequest request) {
        return historicoEscolarService.gerarPorBoletim(matriculaId, request);
    }
}
