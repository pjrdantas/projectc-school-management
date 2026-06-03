package br.com.escola.documento.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.escola.documento.adapter.in.web.dto.DocumentoRequest;
import br.com.escola.documento.adapter.in.web.dto.DocumentoResponse;
import br.com.escola.documento.application.dto.DocumentoInput;
import br.com.escola.documento.application.dto.DocumentoOutput;
import br.com.escola.documento.application.usecase.BuscarDocumentoPorIdUseCase;
import br.com.escola.documento.application.usecase.CriarDocumentoUploadUseCase;
import br.com.escola.documento.application.usecase.CriarDocumentoUseCase;
import br.com.escola.documento.application.usecase.ExcluirDocumentoUseCase;
import br.com.escola.documento.application.usecase.ListarDocumentosPorEntidadeUseCase;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/documentos")
public class DocumentoController {

    private final CriarDocumentoUseCase criarDocumentoUseCase;
    private final CriarDocumentoUploadUseCase criarDocumentoUploadUseCase;
    private final BuscarDocumentoPorIdUseCase buscarDocumentoPorIdUseCase;
    private final ListarDocumentosPorEntidadeUseCase listarDocumentosPorEntidadeUseCase;
    private final ExcluirDocumentoUseCase excluirDocumentoUseCase;

    public DocumentoController(
            CriarDocumentoUseCase criarDocumentoUseCase,
            CriarDocumentoUploadUseCase criarDocumentoUploadUseCase,
            BuscarDocumentoPorIdUseCase buscarDocumentoPorIdUseCase,
            ListarDocumentosPorEntidadeUseCase listarDocumentosPorEntidadeUseCase,
            ExcluirDocumentoUseCase excluirDocumentoUseCase) {
        this.criarDocumentoUseCase = criarDocumentoUseCase;
        this.criarDocumentoUploadUseCase = criarDocumentoUploadUseCase;
        this.buscarDocumentoPorIdUseCase = buscarDocumentoPorIdUseCase;
        this.listarDocumentosPorEntidadeUseCase = listarDocumentosPorEntidadeUseCase;
        this.excluirDocumentoUseCase = excluirDocumentoUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra documento para uma entidade")
    public DocumentoResponse criar(@Valid @RequestBody DocumentoRequest request) {
        return toResponse(criarDocumentoUseCase.executar(new DocumentoInput(
                request.entidadeTipo(),
                request.entidadeId(),
                request.tipoDocumento(),
                request.numeroDocumento(),
                request.caminhoArquivo(),
                request.observacao())));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra documento com upload para uma entidade")
    public DocumentoResponse criarComUpload(
            @RequestParam String entidadeTipo,
            @RequestParam UUID entidadeId,
            @RequestParam String tipoDocumento,
            @RequestParam String numeroDocumento,
            @RequestParam MultipartFile arquivo,
            @RequestParam(required = false) String observacao) {
        return toResponse(criarDocumentoUploadUseCase.executar(
                entidadeTipo,
                entidadeId,
                tipoDocumento,
                numeroDocumento,
                arquivo,
                observacao));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca documento por ID")
    public DocumentoResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return toResponse(buscarDocumentoPorIdUseCase.executar(id));
    }

    @GetMapping
    @Operation(summary = "Lista documentos por entidade")
    public List<DocumentoResponse> listarPorEntidade(
            @RequestParam String entidadeTipo,
            @RequestParam UUID entidadeId) {
        return listarDocumentosPorEntidadeUseCase.executar(entidadeTipo, entidadeId).stream()
                .map(this::toResponse)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui documento")
    public void excluir(@PathVariable @NonNull UUID id) {
        excluirDocumentoUseCase.executar(id);
    }

    private DocumentoResponse toResponse(DocumentoOutput output) {
        return new DocumentoResponse(
                output.id(),
                output.entidadeTipo(),
                output.entidadeId(),
                output.tipoDocumento(),
                output.numeroDocumento(),
                output.caminhoArquivo(),
                output.dataUpload(),
                output.observacao());
    }
}
