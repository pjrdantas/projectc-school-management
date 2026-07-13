package br.com.escola.pedagogicalservice.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.pedagogicalservice.application.context.InternalHeaders;
import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.BoletimResponse;
import br.com.escola.pedagogicalservice.application.dto.HistoricoEscolarTelaResponse;
import br.com.escola.pedagogicalservice.application.port.in.HistoricoEscolarReadUseCase;
import br.com.escola.pedagogicalservice.application.port.in.BoletimQueryUseCase;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class PedagogicalInternalController {

    private final BoletimQueryUseCase boletimQueryUseCase;
    private final HistoricoEscolarReadUseCase historicoEscolarReadUseCase;

    public PedagogicalInternalController(
            BoletimQueryUseCase boletimQueryUseCase,
            HistoricoEscolarReadUseCase historicoEscolarReadUseCase) {
        this.boletimQueryUseCase = boletimQueryUseCase;
        this.historicoEscolarReadUseCase = historicoEscolarReadUseCase;
    }

    @GetMapping("/matriculas/{matriculaId}/boletim")
    public BoletimResponse consultarBoletimPorMatricula(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID matriculaId) {
        return boletimQueryUseCase.consultarBoletimPorMatricula(authorization, context, matriculaId);
    }

    @GetMapping("/matriculas/{matriculaId}/boletim/fechamentos")
    public List<BoletimResponse> listarFechamentosPorMatricula(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID matriculaId) {
        return boletimQueryUseCase.listarFechamentosPorMatricula(authorization, context, matriculaId);
    }

    @GetMapping("/historicos-escolares/novo")
    public HistoricoEscolarTelaResponse carregarHistoricoNovo(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestParam UUID idAluno,
            @RequestParam UUID idMatricula,
            @RequestParam(defaultValue = "CADASTRO") String modo) {
        return historicoEscolarReadUseCase.carregarNovo(authorization, context, idAluno, idMatricula, modo);
    }

    @GetMapping("/historicos-escolares/{id}/carregamento")
    public HistoricoEscolarTelaResponse carregarHistoricoParaEdicao(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return historicoEscolarReadUseCase.carregarParaEdicao(authorization, context, id);
    }
}
