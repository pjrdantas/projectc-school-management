package br.com.escola.enrollmentdocumentservice.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.enrollmentdocumentservice.application.context.InternalRequestContext;
import br.com.escola.enrollmentdocumentservice.application.dto.CriarDocumentoAlunoRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.CriarDocumentoRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.UploadDocumentoAlunoCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.UploadDocumentoCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoAlunoResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoArquivoDownload;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoResponse;

public interface DocumentoUseCase {

    DocumentoAlunoResponse criarDocumentoAluno(
            InternalRequestContext context,
            CriarDocumentoAlunoRequest request);

    DocumentoAlunoResponse enviarDocumentoAluno(
            InternalRequestContext context,
            UploadDocumentoAlunoCommand command);

    DocumentoResponse criarDocumento(InternalRequestContext context, CriarDocumentoRequest request);

    DocumentoResponse enviarDocumento(InternalRequestContext context, UploadDocumentoCommand command);

    DocumentoArquivoDownload baixarDocumentoAluno(InternalRequestContext context, UUID documentoId);

    DocumentoArquivoDownload baixarDocumento(InternalRequestContext context, UUID documentoId);

    void excluirDocumentoAluno(InternalRequestContext context, UUID documentoId);

    void excluirDocumento(InternalRequestContext context, UUID documentoId);

    List<DocumentoAlunoResponse> listarDocumentosPorAluno(InternalRequestContext context, UUID alunoId);

    DocumentoAlunoResponse buscarDocumentoAlunoPorId(InternalRequestContext context, UUID id);

    List<DocumentoResponse> listarDocumentosPorEntidade(
            InternalRequestContext context,
            String entidadeTipo,
            UUID entidadeId);
}
