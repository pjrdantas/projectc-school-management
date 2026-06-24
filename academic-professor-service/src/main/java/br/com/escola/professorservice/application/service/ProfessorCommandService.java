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

@Service
public class ProfessorCommandService implements ProfessorCommandUseCase {

    private final ProfessorCommandWritePort professorWritePort;

    public ProfessorCommandService(ProfessorCommandWritePort professorWritePort) {
        this.professorWritePort = professorWritePort;
    }

    @Override
    public ProfessorResumoResponse criarProfessor(
            String authorization,
            InternalRequestContext context,
            ProfessorCreateRequest request) {
        return professorWritePort.criarProfessor(authorization, context, request);
    }

    @Override
    public ProfessorAlocacaoResponse alocarProfessorTurmaDisciplina(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            ProfessorAllocateRequest request) {
        return professorWritePort.alocarProfessorTurmaDisciplina(authorization, context, professorId, request);
    }
}
