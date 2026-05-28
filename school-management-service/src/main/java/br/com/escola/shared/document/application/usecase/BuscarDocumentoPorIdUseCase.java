package br.com.escola.shared.document.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.shared.document.application.dto.DocumentoOutput;
import br.com.escola.shared.document.application.port.out.DocumentoGateway;

@Service
public class BuscarDocumentoPorIdUseCase {

    private final DocumentoGateway documentoGateway;

    public BuscarDocumentoPorIdUseCase(DocumentoGateway documentoGateway) {
        this.documentoGateway = documentoGateway;
    }

    public DocumentoOutput executar(UUID id) {
        return documentoGateway.findById(id);
    }
}
