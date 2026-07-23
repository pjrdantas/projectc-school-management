package br.com.escola.enrollmentdocumentservice.application.port.out;

import java.util.List;
import java.util.UUID;

import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoAlunoResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoArquivoMetadata;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoArquivoExclusao;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.CriarDocumentoAlunoCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.CriarDocumentoCommand;

public interface DocumentoMetadataPort {

    DocumentoAlunoResponse criarDocumentoAluno(UUID escolaId, CriarDocumentoAlunoCommand command);

    DocumentoResponse criarDocumento(UUID escolaId, CriarDocumentoCommand command);

    DocumentoArquivoMetadata buscarArquivoDocumentoAluno(UUID escolaId, UUID documentoId);

    DocumentoArquivoMetadata buscarArquivoDocumento(UUID escolaId, UUID documentoId);

    DocumentoArquivoExclusao excluirDocumentoAluno(UUID escolaId, UUID documentoId);

    DocumentoArquivoExclusao excluirDocumento(UUID escolaId, UUID documentoId);

    List<DocumentoAlunoResponse> listarDocumentosPorAluno(UUID escolaId, UUID alunoId);

    DocumentoAlunoResponse buscarDocumentoAlunoPorId(UUID escolaId, UUID documentoId);

    List<DocumentoResponse> listarDocumentosPorEntidade(
            UUID escolaId,
            String entidadeTipo,
            UUID entidadeId);
}
