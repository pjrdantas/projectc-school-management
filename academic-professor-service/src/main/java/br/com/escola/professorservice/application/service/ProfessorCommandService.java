package br.com.escola.professorservice.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.ProfessorAllocateRequest;
import br.com.escola.professorservice.application.dto.ProfessorAlocacaoResponse;
import br.com.escola.professorservice.application.dto.ProfessorCreateRequest;
import br.com.escola.professorservice.application.dto.ProfessorResumoResponse;
import br.com.escola.professorservice.application.port.in.ProfessorCommandUseCase;
import br.com.escola.professorservice.application.port.out.ProfessorCommandWritePort;
import br.com.escola.professorservice.application.port.out.ProfessorShadowPersistencePort;

@Service
public class ProfessorCommandService implements ProfessorCommandUseCase {

    private final ProfessorCommandWritePort professorWritePort;
    private final ProfessorShadowPersistencePort professorShadowPersistencePort;

    public ProfessorCommandService(
            ProfessorCommandWritePort professorWritePort,
            ProfessorShadowPersistencePort professorShadowPersistencePort) {
        this.professorWritePort = professorWritePort;
        this.professorShadowPersistencePort = professorShadowPersistencePort;
    }

    @Override
    public ProfessorResumoResponse criarProfessor(
            String authorization,
            InternalRequestContext context,
            ProfessorCreateRequest request) {
        ProfessorResumoResponse response = professorWritePort.criarProfessor(authorization, context, request);
        professorShadowPersistencePort.registrarCriacaoShadow(context, response);
        return response;
    }

    @Override
    public ProfessorAlocacaoResponse alocarProfessorTurmaDisciplina(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            ProfessorAllocateRequest request) {
        ProfessorAlocacaoResponse response =
                professorWritePort.alocarProfessorTurmaDisciplina(authorization, context, professorId, request);
        professorShadowPersistencePort.registrarAlocacaoShadow(context, request, response);
        return response;
    }
}
