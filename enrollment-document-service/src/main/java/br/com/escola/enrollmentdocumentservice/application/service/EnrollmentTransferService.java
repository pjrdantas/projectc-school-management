package br.com.escola.enrollmentdocumentservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.enrollmentdocumentservice.application.context.InternalRequestContext;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoAlunoResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.EscolaOrigemRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.EscolaOrigemResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.MatriculaResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.TransferenciaAlunoRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.TransferenciaAlunoResponse;
import br.com.escola.enrollmentdocumentservice.application.port.in.EnrollmentTransferUseCase;
import br.com.escola.enrollmentdocumentservice.application.port.out.EnrollmentTransferPort;

@Service
public class EnrollmentTransferService implements EnrollmentTransferUseCase {

    private final EnrollmentTransferPort enrollmentTransferPort;

    public EnrollmentTransferService(EnrollmentTransferPort enrollmentTransferPort) {
        this.enrollmentTransferPort = enrollmentTransferPort;
    }

    @Override
    public EscolaOrigemResponse criarEscolaOrigem(
            String authorization,
            InternalRequestContext context,
            EscolaOrigemRequest request) {
        return enrollmentTransferPort.criarEscolaOrigem(authorization, context, request);
    }

    @Override
    public List<EscolaOrigemResponse> listarEscolasOrigem(String authorization, InternalRequestContext context) {
        return enrollmentTransferPort.listarEscolasOrigem(authorization, context);
    }

    @Override
    public EscolaOrigemResponse buscarEscolaOrigem(String authorization, InternalRequestContext context, UUID id) {
        return enrollmentTransferPort.buscarEscolaOrigem(authorization, context, id);
    }

    @Override
    public TransferenciaAlunoResponse criarTransferencia(
            String authorization,
            InternalRequestContext context,
            TransferenciaAlunoRequest request) {
        return enrollmentTransferPort.criarTransferencia(authorization, context, request);
    }

    @Override
    public TransferenciaAlunoResponse buscarTransferencia(String authorization, InternalRequestContext context, UUID id) {
        return enrollmentTransferPort.buscarTransferencia(authorization, context, id);
    }

    @Override
    public List<TransferenciaAlunoResponse> listarTransferenciasPorAluno(
            String authorization,
            InternalRequestContext context,
            UUID alunoId) {
        return enrollmentTransferPort.listarTransferenciasPorAluno(authorization, context, alunoId);
    }

    @Override
    public List<DocumentoAlunoResponse> listarDocumentosPorAluno(
            String authorization,
            InternalRequestContext context,
            UUID alunoId) {
        return enrollmentTransferPort.listarDocumentosPorAluno(authorization, context, alunoId);
    }

    @Override
    public DocumentoAlunoResponse buscarDocumentoAlunoPorId(
            String authorization,
            InternalRequestContext context,
            UUID id) {
        return enrollmentTransferPort.buscarDocumentoAlunoPorId(authorization, context, id);
    }

    @Override
    public List<MatriculaResponse> listarMatriculas(
            String authorization,
            InternalRequestContext context,
            UUID alunoId,
            UUID turmaId,
            UUID periodoLetivoId,
            String status) {
        return enrollmentTransferPort.listarMatriculas(
                authorization,
                context,
                alunoId,
                turmaId,
                periodoLetivoId,
                status);
    }

    @Override
    public MatriculaResponse buscarMatricula(
            String authorization,
            InternalRequestContext context,
            UUID matriculaId) {
        return enrollmentTransferPort.buscarMatricula(authorization, context, matriculaId);
    }

    @Override
    public List<DocumentoResponse> listarDocumentosPorEntidade(
            String authorization,
            InternalRequestContext context,
            String entidadeTipo,
            UUID entidadeId) {
        return enrollmentTransferPort.listarDocumentosPorEntidade(
                authorization,
                context,
                entidadeTipo,
                entidadeId);
    }
}
