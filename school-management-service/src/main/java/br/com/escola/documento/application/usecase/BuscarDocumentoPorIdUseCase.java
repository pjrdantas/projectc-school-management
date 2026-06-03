package br.com.escola.documento.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.documento.application.dto.DocumentoOutput;
import br.com.escola.documento.application.port.out.DocumentoGateway;

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
