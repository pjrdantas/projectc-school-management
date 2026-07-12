package br.com.escola.enrollmentdocumentservice.application.port.out;

import java.util.List;
import java.util.UUID;

import br.com.escola.enrollmentdocumentservice.application.context.InternalRequestContext;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoAlunoResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.EscolaOrigemRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.EscolaOrigemResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.TransferenciaAlunoRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.TransferenciaAlunoResponse;

public interface EnrollmentTransferPort {

    EscolaOrigemResponse criarEscolaOrigem(String authorization, InternalRequestContext context, EscolaOrigemRequest request);

    List<EscolaOrigemResponse> listarEscolasOrigem(String authorization, InternalRequestContext context);

    EscolaOrigemResponse buscarEscolaOrigem(String authorization, InternalRequestContext context, UUID id);

    TransferenciaAlunoResponse criarTransferencia(
            String authorization,
            InternalRequestContext context,
            TransferenciaAlunoRequest request);

    TransferenciaAlunoResponse buscarTransferencia(String authorization, InternalRequestContext context, UUID id);

    List<TransferenciaAlunoResponse> listarTransferenciasPorAluno(
            String authorization,
            InternalRequestContext context,
            UUID alunoId);

    List<DocumentoAlunoResponse> listarDocumentosPorAluno(
            String authorization,
            InternalRequestContext context,
            UUID alunoId);
}
