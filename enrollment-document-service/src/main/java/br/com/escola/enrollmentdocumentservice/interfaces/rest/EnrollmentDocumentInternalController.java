package br.com.escola.enrollmentdocumentservice.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.enrollmentdocumentservice.application.context.InternalHeaders;
import br.com.escola.enrollmentdocumentservice.application.context.InternalRequestContext;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoAlunoResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.EscolaOrigemRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.EscolaOrigemResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.TransferenciaAlunoRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.TransferenciaAlunoResponse;
import br.com.escola.enrollmentdocumentservice.application.port.in.EnrollmentTransferUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class EnrollmentDocumentInternalController {

    private final EnrollmentTransferUseCase enrollmentTransferUseCase;

    public EnrollmentDocumentInternalController(EnrollmentTransferUseCase enrollmentTransferUseCase) {
        this.enrollmentTransferUseCase = enrollmentTransferUseCase;
    }

    @PostMapping("/escolas-origem")
    @ResponseStatus(HttpStatus.CREATED)
    public EscolaOrigemResponse criarEscolaOrigem(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @Valid @RequestBody EscolaOrigemRequest request) {
        return enrollmentTransferUseCase.criarEscolaOrigem(authorization, context, request);
    }

    @GetMapping("/escolas-origem")
    public List<EscolaOrigemResponse> listarEscolasOrigem(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return enrollmentTransferUseCase.listarEscolasOrigem(authorization, context);
    }

    @GetMapping("/escolas-origem/{id}")
    public EscolaOrigemResponse buscarEscolaOrigem(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return enrollmentTransferUseCase.buscarEscolaOrigem(authorization, context, id);
    }

    @PostMapping("/transferencias")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferenciaAlunoResponse criarTransferencia(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @Valid @RequestBody TransferenciaAlunoRequest request) {
        return enrollmentTransferUseCase.criarTransferencia(authorization, context, request);
    }

    @GetMapping("/transferencias/{id}")
    public TransferenciaAlunoResponse buscarTransferencia(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return enrollmentTransferUseCase.buscarTransferencia(authorization, context, id);
    }

    @GetMapping("/transferencias/alunos/{alunoId}")
    public List<TransferenciaAlunoResponse> listarTransferenciasPorAluno(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID alunoId) {
        return enrollmentTransferUseCase.listarTransferenciasPorAluno(authorization, context, alunoId);
    }

    @GetMapping("/documentos-alunos/alunos/{alunoId}")
    public List<DocumentoAlunoResponse> listarDocumentosPorAluno(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID alunoId) {
        return enrollmentTransferUseCase.listarDocumentosPorAluno(authorization, context, alunoId);
    }

    @GetMapping("/documentos-alunos/{id}")
    public DocumentoAlunoResponse buscarDocumentoAlunoPorId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return enrollmentTransferUseCase.buscarDocumentoAlunoPorId(authorization, context, id);
    }
}
