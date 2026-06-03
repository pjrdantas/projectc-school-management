package br.com.escola.documento.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.com.escola.documento.application.dto.DocumentoInput;
import br.com.escola.documento.application.dto.DocumentoOutput;
import br.com.escola.documento.application.usecase.BuscarDocumentoPorIdUseCase;
import br.com.escola.documento.application.usecase.CriarDocumentoUploadUseCase;
import br.com.escola.documento.application.usecase.CriarDocumentoUseCase;
import br.com.escola.documento.application.usecase.ExcluirDocumentoUseCase;
import br.com.escola.documento.application.usecase.ListarDocumentosPorEntidadeUseCase;
import br.com.escola.documento.adapter.in.web.dto.DocumentoAlunoRequest;
import br.com.escola.documento.adapter.in.web.dto.DocumentoAlunoResponse;

@Service
public class DocumentoAlunoService {

    private static final String ENTIDADE_ALUNO = "ALUNO";

    private final CriarDocumentoUseCase criarDocumentoUseCase;
    private final CriarDocumentoUploadUseCase criarDocumentoUploadUseCase;
    private final BuscarDocumentoPorIdUseCase buscarDocumentoPorIdUseCase;
    private final ListarDocumentosPorEntidadeUseCase listarDocumentosPorEntidadeUseCase;
    private final ExcluirDocumentoUseCase excluirDocumentoUseCase;

    public DocumentoAlunoService(
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

    @Transactional
    public DocumentoAlunoResponse criar(DocumentoAlunoRequest request) {
        String numeroDocumento = primeiroValorPreenchido(request.numeroDocumento(), request.nomeArquivo());
        String caminhoArquivo = primeiroValorPreenchido(request.caminhoArquivo(), request.urlArquivo());
        DocumentoOutput output = criarDocumentoUseCase.executar(new DocumentoInput(
                ENTIDADE_ALUNO,
                request.alunoId(),
                request.tipoDocumento(),
                numeroDocumento,
                caminhoArquivo,
                request.observacao()));
        return toAlunoResponse(output);
    }

    @Transactional
    public DocumentoAlunoResponse criarComUpload(
            UUID alunoId,
            String tipoDocumento,
            String numeroDocumento,
            MultipartFile arquivo,
            String observacao) {
        return toAlunoResponse(criarDocumentoUploadUseCase.executar(
                ENTIDADE_ALUNO,
                alunoId,
                tipoDocumento,
                numeroDocumento,
                arquivo,
                observacao));
    }

    @Transactional(readOnly = true)
    public DocumentoAlunoResponse buscarPorId(UUID id) {
        return toAlunoResponse(buscarDocumentoPorIdUseCase.executar(id));
    }

    @Transactional(readOnly = true)
    public List<DocumentoAlunoResponse> listarPorAluno(UUID alunoId) {
        return listarDocumentosPorEntidadeUseCase.executar(ENTIDADE_ALUNO, alunoId).stream()
                .map(this::toAlunoResponse)
                .toList();
    }

    @Transactional
    public void excluir(UUID id) {
        excluirDocumentoUseCase.executar(id);
    }

    private DocumentoAlunoResponse toAlunoResponse(DocumentoOutput output) {
        return new DocumentoAlunoResponse(
                output.id(),
                output.entidadeId(),
                output.tipoDocumento(),
                output.numeroDocumento(),
                output.caminhoArquivo(),
                output.numeroDocumento(),
                output.caminhoArquivo(),
                output.dataUpload(),
                output.observacao());
    }

    private String primeiroValorPreenchido(String principal, String legado) {
        if (principal != null && !principal.isBlank()) {
            return principal.trim();
        }
        return legado == null ? null : legado.trim();
    }
}
