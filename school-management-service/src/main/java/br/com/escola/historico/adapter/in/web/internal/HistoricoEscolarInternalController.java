package br.com.escola.historico.adapter.in.web.internal;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarTelaResponse;
import br.com.escola.historico.application.service.HistoricoEscolarService;

@RestController
@RequestMapping("/internal/historicos-escolares")
public class HistoricoEscolarInternalController {

    private final HistoricoEscolarService historicoEscolarService;

    public HistoricoEscolarInternalController(HistoricoEscolarService historicoEscolarService) {
        this.historicoEscolarService = historicoEscolarService;
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
