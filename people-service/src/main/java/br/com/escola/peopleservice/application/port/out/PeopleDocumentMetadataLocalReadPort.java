package br.com.escola.peopleservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.peopleservice.application.dto.PessoaDocumentoMetadataLocalReadResponse;

public interface PeopleDocumentMetadataLocalReadPort {

    Optional<PessoaDocumentoMetadataLocalReadResponse> buscarDocumentoPorId(UUID documentoId, UUID escolaId);

    List<PessoaDocumentoMetadataLocalReadResponse> listarDocumentosPorPessoa(UUID pessoaId, UUID escolaId);
}
