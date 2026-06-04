package br.com.escola.avaliacao.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.avaliacao.adapter.in.web.dto.NotaAlunoResponse;
import br.com.escola.avaliacao.application.service.AvaliacaoService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/matriculas/{matriculaId}/notas")
public class NotaAlunoController {

    private final AvaliacaoService avaliacaoService;

    public NotaAlunoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping
    @Operation(summary = "Lista notas da matrícula")
    public List<NotaAlunoResponse> listarPorMatricula(@PathVariable @NonNull UUID matriculaId) {
        return avaliacaoService.listarNotasPorMatricula(matriculaId);
    }
}
