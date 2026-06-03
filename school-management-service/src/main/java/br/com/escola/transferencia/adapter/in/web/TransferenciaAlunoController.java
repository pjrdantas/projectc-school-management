package br.com.escola.transferencia.adapter.in.web;

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

import br.com.escola.transferencia.adapter.in.web.dto.TransferenciaAlunoRequest;
import br.com.escola.transferencia.adapter.in.web.dto.TransferenciaAlunoResponse;
import br.com.escola.transferencia.application.service.TransferenciaAlunoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transferencias")
public class TransferenciaAlunoController {

    private final TransferenciaAlunoService transferenciaAlunoService;

    public TransferenciaAlunoController(TransferenciaAlunoService transferenciaAlunoService) {
        this.transferenciaAlunoService = transferenciaAlunoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria transferência de aluno")
    public TransferenciaAlunoResponse criar(@Valid @RequestBody TransferenciaAlunoRequest request) {
        return transferenciaAlunoService.criarTransferencia(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca transferência por ID")
    public TransferenciaAlunoResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return transferenciaAlunoService.buscarTransferencia(id);
    }

    @GetMapping("/alunos/{alunoId}")
    @Operation(summary = "Lista transferências de um aluno")
    public List<TransferenciaAlunoResponse> listarPorAluno(@PathVariable @NonNull UUID alunoId) {
        return transferenciaAlunoService.listarPorAluno(alunoId);
    }
}
