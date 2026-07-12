package br.com.escola.documento.adapter.in.web.internal;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.documento.adapter.in.web.dto.DocumentoAlunoResponse;
import br.com.escola.documento.application.service.DocumentoAlunoService;

@RestController
@RequestMapping("/internal/documentos-alunos")
public class DocumentoAlunoInternalController {

    private final DocumentoAlunoService documentoAlunoService;

    public DocumentoAlunoInternalController(DocumentoAlunoService documentoAlunoService) {
        this.documentoAlunoService = documentoAlunoService;
    }

    @GetMapping("/{id}")
    public DocumentoAlunoResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return documentoAlunoService.buscarPorId(id);
    }

    @GetMapping("/alunos/{alunoId}")
    public List<DocumentoAlunoResponse> listarPorAluno(@PathVariable @NonNull UUID alunoId) {
        return documentoAlunoService.listarPorAluno(alunoId);
    }
}
