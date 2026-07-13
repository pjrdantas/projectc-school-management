package br.com.escola.planningaiservice.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.planningaiservice.application.context.InternalHeaders;
import br.com.escola.planningaiservice.application.context.InternalRequestContext;
import br.com.escola.planningaiservice.application.dto.AprovarVersaoConteudoIaRequest;
import br.com.escola.planningaiservice.application.dto.BibliotecaConteudoPedagogicoResponse;
import br.com.escola.planningaiservice.application.dto.ConteudoIaResponse;
import br.com.escola.planningaiservice.application.dto.ConteudoIaVersaoResponse;
import br.com.escola.planningaiservice.application.dto.CriarVersaoConteudoIaRequest;
import br.com.escola.planningaiservice.application.dto.GerarConteudoIaRequest;
import br.com.escola.planningaiservice.application.dto.PlanejamentoIaInteracaoResponse;
import br.com.escola.planningaiservice.application.port.in.PlanningAiReadUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class PlanningAiInternalController {

    private final PlanningAiReadUseCase planningAiReadUseCase;

    public PlanningAiInternalController(PlanningAiReadUseCase planningAiReadUseCase) {
        this.planningAiReadUseCase = planningAiReadUseCase;
    }

    @GetMapping("/biblioteca-conteudos-pedagogicos")
    public List<BibliotecaConteudoPedagogicoResponse> listarBiblioteca(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestParam(required = false) UUID professorId,
            @RequestParam(required = false) UUID disciplinaId,
            @RequestParam(required = false) String tipoConteudo,
            @RequestParam(required = false) String tema) {
        return planningAiReadUseCase.listarBiblioteca(
                authorization,
                context,
                professorId,
                disciplinaId,
                tipoConteudo,
                tema);
    }

    @PostMapping("/planejamentos-bimestrais/{planejamentoId}/ia/conteudos")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ConteudoIaResponse gerarConteudo(
            @PathVariable UUID planejamentoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @Valid @RequestBody GerarConteudoIaRequest request) {
        return planningAiReadUseCase.gerarConteudo(
                authorization,
                context,
                planejamentoId,
                request);
    }

    @GetMapping("/planejamentos-bimestrais/{planejamentoId}/ia/interacoes")
    public List<PlanejamentoIaInteracaoResponse> listarInteracoes(
            @PathVariable UUID planejamentoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return planningAiReadUseCase.listarInteracoes(
                authorization,
                context,
                planejamentoId);
    }

    @GetMapping("/planejamentos-bimestrais/{planejamentoId}/ia/conteudos")
    public List<ConteudoIaResponse> listarConteudos(
            @PathVariable UUID planejamentoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return planningAiReadUseCase.listarConteudos(
                authorization,
                context,
                planejamentoId);
    }

    @GetMapping("/ia/conteudos/{conteudoId}")
    public ConteudoIaResponse buscarConteudo(
            @PathVariable UUID conteudoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return planningAiReadUseCase.buscarConteudo(
                authorization,
                context,
                conteudoId);
    }

    @PostMapping("/ia/conteudos/{conteudoId}/versoes")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ConteudoIaVersaoResponse criarVersao(
            @PathVariable UUID conteudoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @Valid @RequestBody CriarVersaoConteudoIaRequest request) {
        return planningAiReadUseCase.criarVersao(
                authorization,
                context,
                conteudoId,
                request);
    }

    @PatchMapping("/ia/conteudos/{conteudoId}/aprovar-versao")
    public ConteudoIaResponse aprovarVersao(
            @PathVariable UUID conteudoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @Valid @RequestBody AprovarVersaoConteudoIaRequest request) {
        return planningAiReadUseCase.aprovarVersao(
                authorization,
                context,
                conteudoId,
                request);
    }

    @PostMapping("/ia/conteudos/{conteudoId}/publicar-biblioteca")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public BibliotecaConteudoPedagogicoResponse publicarBiblioteca(
            @PathVariable UUID conteudoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return planningAiReadUseCase.publicarBiblioteca(
                authorization,
                context,
                conteudoId);
    }

    @GetMapping("/ia/conteudos/{conteudoId}/versoes")
    public List<ConteudoIaVersaoResponse> listarVersoes(
            @PathVariable UUID conteudoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return planningAiReadUseCase.listarVersoes(
                authorization,
                context,
                conteudoId);
    }
}
