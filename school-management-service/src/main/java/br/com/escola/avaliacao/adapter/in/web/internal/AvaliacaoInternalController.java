package br.com.escola.avaliacao.adapter.in.web.internal;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.avaliacao.adapter.in.web.dto.AvaliacaoRequest;
import br.com.escola.avaliacao.adapter.in.web.dto.AvaliacaoResponse;
import br.com.escola.avaliacao.application.service.AvaliacaoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal/avaliacoes")
public class AvaliacaoInternalController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoInternalController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AvaliacaoResponse criar(@Valid @RequestBody AvaliacaoRequest request) {
        return avaliacaoService.criar(request);
    }

    @GetMapping
    public List<AvaliacaoResponse> listar(
            @RequestParam(required = false) UUID professorTurmaDisciplinaId,
            @RequestParam(required = false) UUID turmaId) {
        return avaliacaoService.listar(professorTurmaDisciplinaId, turmaId);
    }

    @GetMapping("/{id}")
    public AvaliacaoResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return avaliacaoService.buscarPorId(id);
    }
}
