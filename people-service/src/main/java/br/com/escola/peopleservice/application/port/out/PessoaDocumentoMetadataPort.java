package br.com.escola.peopleservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.peopleservice.application.dto.PessoaDocumentoMetadataResponse;

public interface PessoaDocumentoMetadataPort {

    Optional<PessoaDocumentoMetadataResponse> buscarDocumentoPorId(UUID documentoId, UUID escolaId);

    List<PessoaDocumentoMetadataResponse> listarDocumentosPorPessoa(UUID pessoaId, UUID escolaId);
}
