package br.com.escola.shared.document.application.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.shared.document.application.dto.DocumentoOutput;
import br.com.escola.shared.document.application.port.out.DocumentoGateway;
import br.com.escola.shared.document.domain.EntidadeDocumentalTipo;

@Service
public class ListarDocumentosPorEntidadeUseCase {

    private final DocumentoGateway documentoGateway;

    public ListarDocumentosPorEntidadeUseCase(DocumentoGateway documentoGateway) {
        this.documentoGateway = documentoGateway;
    }

    public List<DocumentoOutput> executar(String entidadeTipoRaw, UUID entidadeId) {
        EntidadeDocumentalTipo entidadeTipo = DocumentoUseCaseSupport.parseEntidadeTipo(entidadeTipoRaw);
        return documentoGateway.findByEntidade(entidadeTipo, entidadeId);
    }
}
