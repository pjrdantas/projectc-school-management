package br.com.escola.enrollmentdocumentservice.interfaces.rest;

import java.util.List;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import br.com.escola.enrollmentdocumentservice.application.context.InternalHeaders;
import br.com.escola.enrollmentdocumentservice.application.context.InternalRequestContext;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoAlunoResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoArquivoDownload;
import br.com.escola.enrollmentdocumentservice.application.dto.CriarDocumentoRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.CriarDocumentoAlunoRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.UploadDocumentoAlunoCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.UploadDocumentoCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.EscolaOrigemRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.EscolaOrigemResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.MatriculaResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.TransferenciaAlunoRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.TransferenciaAlunoResponse;
import br.com.escola.enrollmentdocumentservice.application.port.in.DocumentoUseCase;
import br.com.escola.enrollmentdocumentservice.application.port.in.EnrollmentTransferUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class DocumentoMatriculaInternalController {

    private final EnrollmentTransferUseCase enrollmentTransferUseCase;
    private final DocumentoUseCase documentoUseCase;

    public DocumentoMatriculaInternalController(
            EnrollmentTransferUseCase enrollmentTransferUseCase,
            DocumentoUseCase documentoUseCase) {
        this.enrollmentTransferUseCase = enrollmentTransferUseCase;
        this.documentoUseCase = documentoUseCase;
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
        return documentoUseCase.listarDocumentosPorAluno(context, alunoId);
    }

    @PostMapping("/documentos-alunos")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentoAlunoResponse criarDocumentoAluno(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @Valid @RequestBody CriarDocumentoAlunoRequest request) {
        return documentoUseCase.criarDocumentoAluno(context, request);
    }

    @PostMapping(value = "/documentos-alunos/upload", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentoAlunoResponse enviarDocumentoAluno(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestParam UUID alunoId,
            @RequestParam String tipoDocumento,
            @RequestParam(required = false) String numeroDocumento,
            @RequestParam(required = false) String observacao,
            @RequestParam MultipartFile arquivo) throws java.io.IOException {
        return documentoUseCase.enviarDocumentoAluno(
                context,
                new UploadDocumentoAlunoCommand(
                        alunoId,
                        tipoDocumento,
                        numeroDocumento,
                        observacao,
                        arquivo.getOriginalFilename(),
                        arquivo.getContentType(),
                        arquivo.getSize(),
                        arquivo.getInputStream()));
    }

    @PostMapping("/documentos")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentoResponse criarDocumento(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @Valid @RequestBody CriarDocumentoRequest request) {
        return documentoUseCase.criarDocumento(context, request);
    }

    @PostMapping(value = "/documentos/upload", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentoResponse enviarDocumento(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestParam String entidadeTipo,
            @RequestParam UUID entidadeId,
            @RequestParam String tipoDocumento,
            @RequestParam(required = false) String numeroDocumento,
            @RequestParam(required = false) String observacao,
            @RequestParam MultipartFile arquivo) throws java.io.IOException {
        return documentoUseCase.enviarDocumento(
                context,
                new UploadDocumentoCommand(
                        entidadeTipo,
                        entidadeId,
                        tipoDocumento,
                        numeroDocumento,
                        observacao,
                        arquivo.getOriginalFilename(),
                        arquivo.getContentType(),
                        arquivo.getSize(),
                        arquivo.getInputStream()));
    }

    @GetMapping("/documentos-alunos/{id}")
    public DocumentoAlunoResponse buscarDocumentoAlunoPorId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return documentoUseCase.buscarDocumentoAlunoPorId(context, id);
    }

    @GetMapping("/documentos-alunos/{id}/conteudo")
    public ResponseEntity<StreamingResponseBody> baixarDocumentoAluno(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return download(documentoUseCase.baixarDocumentoAluno(context, id));
    }

    @DeleteMapping("/documentos-alunos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirDocumentoAluno(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        documentoUseCase.excluirDocumentoAluno(context, id);
    }

    @GetMapping("/matriculas")
    public List<MatriculaResponse> listarMatriculas(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @org.springframework.web.bind.annotation.RequestParam(required = false) UUID alunoId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) UUID turmaId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) UUID periodoLetivoId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String status) {
        return enrollmentTransferUseCase.listarMatriculas(
                authorization,
                context,
                alunoId,
                turmaId,
                periodoLetivoId,
                status);
    }

    @GetMapping("/matriculas/{matriculaId}")
    public MatriculaResponse buscarMatricula(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID matriculaId) {
        return enrollmentTransferUseCase.buscarMatricula(authorization, context, matriculaId);
    }

    @GetMapping("/documentos")
    public List<DocumentoResponse> listarDocumentosPorEntidade(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @org.springframework.web.bind.annotation.RequestParam String entidadeTipo,
            @org.springframework.web.bind.annotation.RequestParam UUID entidadeId) {
        return documentoUseCase.listarDocumentosPorEntidade(
                context,
                entidadeTipo,
                entidadeId);
    }

    @GetMapping("/documentos/{id}/conteudo")
    public ResponseEntity<StreamingResponseBody> baixarDocumento(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return download(documentoUseCase.baixarDocumento(context, id));
    }

    @DeleteMapping("/documentos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirDocumento(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        documentoUseCase.excluirDocumento(context, id);
    }

    private ResponseEntity<StreamingResponseBody> download(DocumentoArquivoDownload arquivo) {
        StreamingResponseBody corpo = output -> {
            try (var input = arquivo.conteudo()) {
                input.transferTo(output);
            }
        };
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(arquivo.tipoConteudo()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(arquivo.nomeArquivo(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(corpo);
    }
}

