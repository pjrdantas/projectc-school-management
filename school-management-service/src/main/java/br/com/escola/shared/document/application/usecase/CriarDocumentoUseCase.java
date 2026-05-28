package br.com.escola.shared.document.application.usecase;

import org.springframework.stereotype.Service;

import br.com.escola.shared.document.application.dto.DocumentoInput;
import br.com.escola.shared.document.application.dto.DocumentoOutput;
import br.com.escola.shared.document.application.port.out.DocumentoGateway;
import br.com.escola.shared.document.domain.EntidadeDocumentalTipo;
import br.com.escola.shared.document.domain.TipoDocumento;

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
