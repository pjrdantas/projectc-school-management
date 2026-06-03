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

import br.com.escola.transferencia.adapter.in.web.dto.EscolaOrigemRequest;
import br.com.escola.transferencia.adapter.in.web.dto.EscolaOrigemResponse;
import br.com.escola.transferencia.application.service.TransferenciaAlunoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/escolas-origem")
public class EscolaOrigemController {

    private final TransferenciaAlunoService transferenciaAlunoService;

    public EscolaOrigemController(TransferenciaAlunoService transferenciaAlunoService) {
        this.transferenciaAlunoService = transferenciaAlunoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra escola de origem")
    public EscolaOrigemResponse criar(@Valid @RequestBody EscolaOrigemRequest request) {
        return transferenciaAlunoService.criarEscolaOrigem(request);
    }

    @GetMapping
    @Operation(summary = "Lista escolas de origem")
    public List<EscolaOrigemResponse> listar() {
        return transferenciaAlunoService.listarEscolasOrigem();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca escola de origem por ID")
    public EscolaOrigemResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return transferenciaAlunoService.buscarEscolaOrigem(id);
    }
}
