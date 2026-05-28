package br.com.escola.shared.document.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.shared.document.application.port.out.DocumentoGateway;

@Service
public class ExcluirDocumentoUseCase {

    private final DocumentoGateway documentoGateway;

    public ExcluirDocumentoUseCase(DocumentoGateway documentoGateway) {
        this.documentoGateway = documentoGateway;
    }

    public void executar(UUID id) {
        documentoGateway.deleteById(id);
    }
}
