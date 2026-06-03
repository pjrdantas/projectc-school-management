package br.com.escola.documento.application.usecase;

import org.springframework.stereotype.Service;

import br.com.escola.documento.application.dto.DocumentoInput;
import br.com.escola.documento.application.dto.DocumentoOutput;
import br.com.escola.documento.application.port.out.DocumentoGateway;
import br.com.escola.documento.domain.EntidadeDocumentalTipo;
import br.com.escola.documento.domain.TipoDocumento;

@Service
public class CriarDocumentoUseCase {

    private final DocumentoGateway documentoGateway;

    public CriarDocumentoUseCase(DocumentoGateway documentoGateway) {
        this.documentoGateway = documentoGateway;
    }

    public DocumentoOutput executar(DocumentoInput input) {
        EntidadeDocumentalTipo entidadeTipo = DocumentoUseCaseSupport.parseEntidadeTipo(input.entidadeTipo());
        TipoDocumento tipoDocumento = DocumentoUseCaseSupport.parseTipoDocumento(input.tipoDocumento());
        DocumentoUseCaseSupport.validarCampoObrigatorio(input.numeroDocumento(), "Número do documento é obrigatório");
        DocumentoUseCaseSupport.validarCampoObrigatorio(input.caminhoArquivo(), "Arquivo do documento é obrigatório");
        return documentoGateway.save(input, entidadeTipo, tipoDocumento);
    }
}
