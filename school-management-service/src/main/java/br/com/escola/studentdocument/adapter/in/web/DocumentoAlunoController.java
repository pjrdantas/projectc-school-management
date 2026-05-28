package br.com.escola.studentdocument.adapter.in.web;

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

import br.com.escola.studentdocument.adapter.in.web.dto.DocumentoAlunoRequest;
import br.com.escola.studentdocument.adapter.in.web.dto.DocumentoAlunoResponse;
import br.com.escola.studentdocument.application.service.DocumentoAlunoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/documentos-alunos")
public class DocumentoAlunoController {

    private final DocumentoAlunoService documentoAlunoService;

    public DocumentoAlunoController(DocumentoAlunoService documentoAlunoService) {
        this.documentoAlunoService = documentoAlunoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra documento do aluno")
    public DocumentoAlunoResponse criar(@Valid @RequestBody DocumentoAlunoRequest request) {
        return documentoAlunoService.criar(request);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra documento do aluno com upload de arquivo")
    public DocumentoAlunoResponse criarComUpload(
            @RequestParam UUID alunoId,
            @RequestParam String tipoDocumento,
            @RequestParam String numeroDocumento,
            @RequestParam MultipartFile arquivo,
            @RequestParam(required = false) String observacao) {
        return documentoAlunoService.criarComUpload(alunoId, tipoDocumento, numeroDocumento, arquivo, observacao);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca documento do aluno por ID")
    public DocumentoAlunoResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return documentoAlunoService.buscarPorId(id);
    }

    @GetMapping("/alunos/{alunoId}")
    @Operation(summary = "Lista documentos de um aluno")
    public List<DocumentoAlunoResponse> listarPorAluno(@PathVariable @NonNull UUID alunoId) {
        return documentoAlunoService.listarPorAluno(alunoId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui documento do aluno")
    public void excluir(@PathVariable @NonNull UUID id) {
        documentoAlunoService.excluir(id);
    }
}
