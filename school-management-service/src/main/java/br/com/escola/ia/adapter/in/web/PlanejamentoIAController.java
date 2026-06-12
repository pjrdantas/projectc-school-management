package br.com.escola.ia.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.ia.adapter.in.web.dto.AprovarVersaoConteudoIARequest;
import br.com.escola.ia.adapter.in.web.dto.BibliotecaConteudoPedagogicoResponse;
import br.com.escola.ia.adapter.in.web.dto.ConteudoIAResponse;
import br.com.escola.ia.adapter.in.web.dto.ConteudoIAVersaoResponse;
import br.com.escola.ia.adapter.in.web.dto.CriarVersaoConteudoIARequest;
import br.com.escola.ia.adapter.in.web.dto.GerarConteudoIARequest;
import br.com.escola.ia.adapter.in.web.dto.PlanejamentoIAInteracaoResponse;
import br.com.escola.ia.application.service.PlanejamentoIAService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class PlanejamentoIAController {

    private final PlanejamentoIAService planejamentoIAService;

    public PlanejamentoIAController(PlanejamentoIAService planejamentoIAService) {
        this.planejamentoIAService = planejamentoIAService;
    }

    @PostMapping("/planejamentos-bimestrais/{planejamentoId}/ia/conteudos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Gera sugestão de conteúdo pedagógico em modo interno")
    public ConteudoIAResponse gerarConteudo(
            @PathVariable @NonNull UUID planejamentoId,
            @Valid @RequestBody GerarConteudoIARequest request) {
        return planejamentoIAService.gerarConteudo(planejamentoId, request);
    }

    @GetMapping("/planejamentos-bimestrais/{planejamentoId}/ia/interacoes")
    @Operation(summary = "Lista interações de IA do planejamento")
    public List<PlanejamentoIAInteracaoResponse> listarInteracoes(@PathVariable @NonNull UUID planejamentoId) {
        return planejamentoIAService.listarInteracoes(planejamentoId);
    }

    @GetMapping("/planejamentos-bimestrais/{planejamentoId}/ia/conteudos")
    @Operation(summary = "Lista conteúdos gerados do planejamento")
    public List<ConteudoIAResponse> listarConteudos(@PathVariable @NonNull UUID planejamentoId) {
        return planejamentoIAService.listarConteudos(planejamentoId);
    }

    @GetMapping("/ia/conteudos/{conteudoId}")
    @Operation(summary = "Busca conteúdo gerado por ID")
    public ConteudoIAResponse buscarConteudo(@PathVariable @NonNull UUID conteudoId) {
        return planejamentoIAService.buscarConteudo(conteudoId);
    }

    @PostMapping("/ia/conteudos/{conteudoId}/versoes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria nova versão do conteúdo gerado")
    public ConteudoIAVersaoResponse criarVersao(
            @PathVariable @NonNull UUID conteudoId,
            @Valid @RequestBody CriarVersaoConteudoIARequest request) {
        return planejamentoIAService.criarVersao(conteudoId, request);
    }

    @GetMapping("/ia/conteudos/{conteudoId}/versoes")
    @Operation(summary = "Lista versões do conteúdo gerado")
    public List<ConteudoIAVersaoResponse> listarVersoes(@PathVariable @NonNull UUID conteudoId) {
        return planejamentoIAService.listarVersoes(conteudoId);
    }

    @PatchMapping("/ia/conteudos/{conteudoId}/aprovar-versao")
    @Operation(summary = "Aprova uma versão do conteúdo gerado")
    public ConteudoIAResponse aprovarVersao(
            @PathVariable @NonNull UUID conteudoId,
            @Valid @RequestBody AprovarVersaoConteudoIARequest request) {
        return planejamentoIAService.aprovarVersao(conteudoId, request);
    }

    @PostMapping("/ia/conteudos/{conteudoId}/publicar-biblioteca")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Publica conteúdo aprovado na biblioteca pedagógica")
    public BibliotecaConteudoPedagogicoResponse publicarBiblioteca(@PathVariable @NonNull UUID conteudoId) {
        return planejamentoIAService.publicarBiblioteca(conteudoId);
    }

    @GetMapping("/biblioteca-conteudos-pedagogicos")
    @Operation(summary = "Lista biblioteca de conteúdo pedagógico")
    public List<BibliotecaConteudoPedagogicoResponse> listarBiblioteca(
            @RequestParam(required = false) UUID professorId,
            @RequestParam(required = false) UUID disciplinaId,
            @RequestParam(required = false) String tipoConteudo,
            @RequestParam(required = false) String tema) {
        return planejamentoIAService.listarBiblioteca(professorId, disciplinaId, tipoConteudo, tema);
    }
}
