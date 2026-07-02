package br.com.escola.documento.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.escola.documento.application.dto.DocumentoInput;
import br.com.escola.documento.application.dto.DocumentoOutput;
import br.com.escola.documento.application.port.out.DocumentoArquivoReferencia;
import br.com.escola.documento.application.port.out.DocumentoArquivoStorage;
import br.com.escola.documento.domain.EntidadeDocumentalTipo;
import br.com.escola.documento.domain.exception.DocumentoInvalidoException;

@Service
public class CriarDocumentoUploadUseCase {

    private final DocumentoArquivoStorage documentoArquivoStorage;
    private final CriarDocumentoUseCase criarDocumentoUseCase;

    public CriarDocumentoUploadUseCase(
            DocumentoArquivoStorage documentoArquivoStorage,
            CriarDocumentoUseCase criarDocumentoUseCase) {
        this.documentoArquivoStorage = documentoArquivoStorage;
        this.criarDocumentoUseCase = criarDocumentoUseCase;
    }

    public DocumentoOutput executar(
            String entidadeTipoRaw,
            UUID entidadeId,
            String tipoDocumento,
            String numeroDocumento,
            MultipartFile arquivo,
            String observacao) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new DocumentoInvalidoException("Arquivo do documento é obrigatório");
        }

        EntidadeDocumentalTipo entidadeTipo = DocumentoUseCaseSupport.parseEntidadeTipo(entidadeTipoRaw);
        DocumentoArquivoReferencia referenciaArquivo = documentoArquivoStorage.salvar(entidadeTipo, entidadeId, arquivo);
        return criarDocumentoUseCase.executar(new DocumentoInput(
                entidadeTipo.name(),
                entidadeId,
                tipoDocumento,
                numeroDocumento,
                referenciaArquivo.caminhoPersistencia(),
                observacao));
    }
}
