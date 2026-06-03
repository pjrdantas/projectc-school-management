package br.com.escola.documento.application.port.out;

import java.util.List;
import java.util.UUID;

import br.com.escola.documento.application.dto.DocumentoInput;
import br.com.escola.documento.application.dto.DocumentoOutput;
import br.com.escola.documento.domain.EntidadeDocumentalTipo;
import br.com.escola.documento.domain.TipoDocumento;

public interface DocumentoGateway {

    DocumentoOutput save(DocumentoInput input, EntidadeDocumentalTipo entidadeTipo, TipoDocumento tipoDocumento);

    DocumentoOutput findById(UUID id);

    List<DocumentoOutput> findByEntidade(EntidadeDocumentalTipo entidadeTipo, UUID entidadeId);

    void deleteById(UUID id);

    void deleteByEntidade(EntidadeDocumentalTipo entidadeTipo, UUID entidadeId);
}
