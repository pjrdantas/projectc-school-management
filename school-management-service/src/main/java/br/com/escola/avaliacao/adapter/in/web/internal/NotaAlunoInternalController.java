package br.com.escola.avaliacao.adapter.in.web.internal;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.avaliacao.adapter.in.web.dto.NotaAlunoResponse;
import br.com.escola.avaliacao.application.service.AvaliacaoService;

@RestController
@RequestMapping("/internal/matriculas/{matriculaId}/notas")
public class NotaAlunoInternalController {

    private final AvaliacaoService avaliacaoService;

    public NotaAlunoInternalController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping
    public List<NotaAlunoResponse> listarPorMatricula(@PathVariable @NonNull UUID matriculaId) {
        return avaliacaoService.listarNotasPorMatricula(matriculaId);
    }
}
