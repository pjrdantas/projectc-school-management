package br.com.escola.historico.adapter.in.web.internal;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarRequest;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarResponse;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarTelaResponse;
import br.com.escola.historico.application.service.HistoricoEscolarService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal/historicos-escolares")
public class HistoricoEscolarInternalController {

    private final HistoricoEscolarService historicoEscolarService;

    public HistoricoEscolarInternalController(HistoricoEscolarService historicoEscolarService) {
        this.historicoEscolarService = historicoEscolarService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HistoricoEscolarResponse criar(@Valid @RequestBody HistoricoEscolarRequest request) {
        return historicoEscolarService.criar(request);
    }

    @PutMapping("/{id}")
    public HistoricoEscolarResponse atualizar(
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody HistoricoEscolarRequest request) {
        return historicoEscolarService.atualizar(id, request);
    }

    @GetMapping("/novo")
    public HistoricoEscolarTelaResponse carregarNovo(
            @RequestParam UUID idAluno,
            @RequestParam UUID idMatricula,
            @RequestParam(defaultValue = "CADASTRO") String modo) {
        return historicoEscolarService.carregarNovo(idAluno, idMatricula, modo);
    }

    @GetMapping("/{id}/carregamento")
    public HistoricoEscolarTelaResponse carregarParaEdicao(@PathVariable @NonNull UUID id) {
        return historicoEscolarService.carregarParaEdicao(id);
    }
}
