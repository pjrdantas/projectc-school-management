package br.com.escola.pedagogicalservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.AulaResponse;
import br.com.escola.pedagogicalservice.application.dto.FrequenciaAlunoResponse;
import br.com.escola.pedagogicalservice.application.dto.FrequenciaDocenteResponse;
import br.com.escola.pedagogicalservice.application.port.in.AulaUseCase;
import br.com.escola.pedagogicalservice.application.port.out.AulaPort;

@Service
public class AulaService implements AulaUseCase {

    private final AulaPort aulaPort;

    public AulaService(AulaPort aulaPort) {
        this.aulaPort = aulaPort;
    }

    @Override
    public AulaResponse criar(String authorization, InternalRequestContext context, String requestBody) {
        return aulaPort.criar(authorization, context, requestBody);
    }

    @Override
    public List<AulaResponse> listar(
            String authorization,
            InternalRequestContext context,
            UUID professorTurmaDisciplinaId,
            UUID turmaId) {
        return aulaPort.listar(authorization, context, professorTurmaDisciplinaId, turmaId);
    }

    @Override
    public AulaResponse buscarPorId(String authorization, InternalRequestContext context, UUID aulaId) {
        return aulaPort.buscarPorId(authorization, context, aulaId);
    }

    @Override
    public FrequenciaDocenteResponse registrarFrequenciaProfessor(
            String authorization,
            InternalRequestContext context,
            UUID aulaId,
            String requestBody) {
        return aulaPort.registrarFrequenciaProfessor(authorization, context, aulaId, requestBody);
    }

    @Override
    public List<FrequenciaDocenteResponse> listarFrequenciaProfessor(
            String authorization,
            InternalRequestContext context,
            UUID aulaId) {
        return aulaPort.listarFrequenciaProfessor(authorization, context, aulaId);
    }

    @Override
    public FrequenciaAlunoResponse registrarFrequenciaAluno(
            String authorization,
            InternalRequestContext context,
            UUID aulaId,
            String requestBody) {
        return aulaPort.registrarFrequenciaAluno(authorization, context, aulaId, requestBody);
    }

    @Override
    public List<FrequenciaAlunoResponse> listarFrequenciasAlunos(
            String authorization,
            InternalRequestContext context,
            UUID aulaId) {
        return aulaPort.listarFrequenciasAlunos(authorization, context, aulaId);
    }
}

