package br.com.escola.professorservice.application.service;

import org.springframework.stereotype.Service;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.ProfessorCreateRequest;
import br.com.escola.professorservice.application.dto.ProfessorResumoResponse;
import br.com.escola.professorservice.application.port.in.ProfessorCommandUseCase;
import br.com.escola.professorservice.application.port.out.ProfessorWritePort;

@Service
public class ProfessorCommandService implements ProfessorCommandUseCase {

    private final ProfessorWritePort professorWritePort;

    public ProfessorCommandService(ProfessorWritePort professorWritePort) {
        this.professorWritePort = professorWritePort;
    }

    @Override
    public ProfessorResumoResponse criarProfessor(
            String authorization,
            InternalRequestContext context,
            ProfessorCreateRequest request) {
        return professorWritePort.criarProfessor(authorization, context, request);
    }
}
