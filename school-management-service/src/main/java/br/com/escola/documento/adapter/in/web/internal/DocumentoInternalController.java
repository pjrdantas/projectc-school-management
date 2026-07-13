package br.com.escola.documento.adapter.in.web.internal;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.documento.application.dto.DocumentoOutput;
import br.com.escola.documento.application.usecase.ListarDocumentosPorEntidadeUseCase;

@RestController
@RequestMapping("/internal/documentos")
public class DocumentoInternalController {

    private final ListarDocumentosPorEntidadeUseCase listarDocumentosPorEntidadeUseCase;

    public DocumentoInternalController(ListarDocumentosPorEntidadeUseCase listarDocumentosPorEntidadeUseCase) {
        this.listarDocumentosPorEntidadeUseCase = listarDocumentosPorEntidadeUseCase;
    }

    @GetMapping
    public List<DocumentoInternalResponse> listarPorEntidade(
            @RequestParam String entidadeTipo,
            @RequestParam UUID entidadeId) {
        return listarDocumentosPorEntidadeUseCase.executar(entidadeTipo, entidadeId).stream()
                .map(this::toResponse)
                .toList();
    }

    private DocumentoInternalResponse toResponse(DocumentoOutput output) {
        return new DocumentoInternalResponse(
                output.id(),
                output.entidadeTipo(),
                output.entidadeId(),
                output.escolaId(),
                output.escolaNome(),
                output.tipoDocumento(),
                output.numeroDocumento(),
                output.caminhoArquivo(),
                output.dataUpload(),
                output.observacao());
    }
}
