package br.com.escola.pedagogicalservice.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.pedagogicalservice.application.context.InternalHeaders;
import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.BoletimResponse;
import br.com.escola.pedagogicalservice.application.dto.HistoricoEscolarTelaResponse;
import br.com.escola.pedagogicalservice.application.port.in.HistoricoEscolarReadUseCase;
import br.com.escola.pedagogicalservice.application.port.in.HistoricoEscolarWriteUseCase;
import br.com.escola.pedagogicalservice.application.port.in.BoletimQueryUseCase;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class PedagogicalInternalController {

    private final BoletimQueryUseCase boletimQueryUseCase;
    private final HistoricoEscolarReadUseCase historicoEscolarReadUseCase;
    private final HistoricoEscolarWriteUseCase historicoEscolarWriteUseCase;

    public PedagogicalInternalController(
            BoletimQueryUseCase boletimQueryUseCase,
            HistoricoEscolarReadUseCase historicoEscolarReadUseCase,
            HistoricoEscolarWriteUseCase historicoEscolarWriteUseCase) {
        this.boletimQueryUseCase = boletimQueryUseCase;
        this.historicoEscolarReadUseCase = historicoEscolarReadUseCase;
        this.historicoEscolarWriteUseCase = historicoEscolarWriteUseCase;
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

    @PostMapping("/historicos-escolares")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> criarHistoricoEscolar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestBody String requestBody) {
        return historicoEscolarWriteUseCase.criar(authorization, context, requestBody);
    }

    @PutMapping("/historicos-escolares/{id}")
    public ResponseEntity<String> atualizarHistoricoEscolar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id,
            @RequestBody String requestBody) {
        return historicoEscolarWriteUseCase.atualizar(authorization, context, id, requestBody);
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
