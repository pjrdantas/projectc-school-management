package br.com.escola.documento.application.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.documento.application.dto.DocumentoOutput;
import br.com.escola.documento.application.port.out.DocumentoGateway;
import br.com.escola.documento.domain.EntidadeDocumentalTipo;

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
